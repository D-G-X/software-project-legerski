package de.hft.licensing.rest;

import de.hft.licensing.api.ApplicationsApi;
import de.hft.licensing.db.tables.ApplicationPayment;
import de.hft.licensing.model.ApplicationCreate;
import de.hft.licensing.model.ApplicationDocumentRecord;
import de.hft.licensing.model.ApplicationPaymentCreate;
import de.hft.licensing.model.ApplicationPaymentRecord;
import de.hft.licensing.model.ApplicationRecord;
import de.hft.licensing.model.ApplicationUpdate;
import de.hft.licensing.model.RunLottery200Response;
import de.hft.licensing.model.RunLotteryRequest;
import de.hft.licensing.model.UpdatePaymentRequest;
import de.hft.licensing.model.VerifyDocumentRequest;
import de.hft.licensing.model.PaymentStatusEnum;
import de.hft.licensing.model.ApplicationStatusEnum;
import de.hft.licensing.model.LicenseTypeEnum;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.tables.User;
import org.jooq.DSLContext;
import de.hft.licensing.db.tables.Application;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
public class ApplicationController implements ApplicationsApi {

    private final DSLContext dsl;

    public ApplicationController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ResponseEntity<ApplicationRecord> createApplication(ApplicationCreate applicationCreate) {
        if (applicationCreate == null || applicationCreate.getUserId() == null || applicationCreate.getLicenseType() == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean userExists = dsl.fetchExists(
                dsl.selectOne()
                        .from(User.USER)
                        .where(User.USER.ID.eq(applicationCreate.getUserId().toString()))
        );
        if (!userExists) {
            // client provided a user_id that does not exist
            return ResponseEntity.status(422).build();
        }

        LocalDateTime now = LocalDateTime.now();

        LicenseType dbLicenseType;
        try {
            dbLicenseType = LicenseType.valueOf(applicationCreate.getLicenseType().name().toLowerCase());
        } catch (IllegalArgumentException ignored) {
            try {
                dbLicenseType = LicenseType.valueOf(applicationCreate.getLicenseType().name());
            } catch (IllegalArgumentException ignored2) {
                dbLicenseType = LicenseType.lookupLiteral(applicationCreate.getLicenseType().toString());
            }
        }
        if (dbLicenseType == null) {
            return ResponseEntity.badRequest().build();
        }

        // insert and return DB record (jooq DB record, not API model record)
        var dbRecord = dsl.insertInto(Application.APPLICATION)
                .set(Application.APPLICATION.USER_ID, applicationCreate.getUserId().toString())
                .set(Application.APPLICATION.APPLICATION_STATUS, ApplicationStatus.draft)
                .set(Application.APPLICATION.APPLIED_AT, now)
                .set(Application.APPLICATION.CHANGED_AT, now)
                .set(Application.APPLICATION.CADASTRAL_REFERENCE, applicationCreate.getCadastralReference())
                .set(Application.APPLICATION.LICENSE_TYPE, dbLicenseType)
                .set(Application.APPLICATION.REMARKS, applicationCreate.getRemarks())
                .returning()
                .fetchOne();

        if (dbRecord == null) {
            return ResponseEntity.status(500).build();
        }

        // map DB record -> API model and convert enums explicitly
        ApplicationRecord api = new ApplicationRecord();
        api.setId(dbRecord.getId());
        api.setUserId(UUID.fromString(dbRecord.getUserId()));
        api.setCadastralReference(dbRecord.getCadastralReference());
        api.setAppliedAt(dbRecord.getAppliedAt().atOffset(ZoneOffset.UTC));
        api.setChangedAt(dbRecord.getChangedAt().atOffset(ZoneOffset.UTC));
        api.setRemarks(dbRecord.getRemarks());

        if (dbRecord.getLicenseType() != null) {
            String dbName = dbRecord.getLicenseType().name();
            for (LicenseTypeEnum lt : LicenseTypeEnum.values()) {
                if (lt.name().equalsIgnoreCase(dbName)) {
                    api.setLicenseType(lt);
                    break;
                }
            }
        }
        if (dbRecord.getApplicationStatus() != null) {
            String dbName = dbRecord.getApplicationStatus().name();
            for (ApplicationStatusEnum st : ApplicationStatusEnum.values()) {
                if (st.name().equalsIgnoreCase(dbName)) {
                    api.setApplicationStatus(st);
                    break;
                }
            }
        }

        return ResponseEntity.created(URI.create("/applications/" + api.getId())).body(api);
    }


    @Override
    public ResponseEntity<ApplicationPaymentRecord> createPayment(Integer applicationId, ApplicationPaymentCreate applicationPaymentCreate) {
        if (applicationId == null || applicationPaymentCreate.getApplicationId() == null || !Objects.equals(applicationPaymentCreate.getApplicationId(), applicationId)) {
            return ResponseEntity.badRequest().build();
        }
        LocalDateTime now = LocalDateTime.now();
        var dbPayment = dsl.insertInto(ApplicationPayment.APPLICATION_PAYMENT)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_DATE, now)
                .set(ApplicationPayment.APPLICATION_PAYMENT.AMOUNT, applicationPaymentCreate.getAmount())
                .set(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID, applicationPaymentCreate.getApplicationId())
              //  .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS, applicationPaymentCreate.getPaymentStatus())
                .returning()
                .fetchOne();
        if(dbPayment == null) {
            return ResponseEntity.status(500).build();
        }

        ApplicationPaymentRecord apiPayment = new ApplicationPaymentRecord(
                dbPayment.getId(),
                applicationId,
                dbPayment.getAmount(),
                PaymentStatusEnum.PAID
        );
        apiPayment.setPaymentDate(dbPayment.getPaymentDate().atOffset(ZoneOffset.UTC));


        return ResponseEntity.created(URI.create("/applications/" + applicationId + "/payments/" + apiPayment.getId())).body(apiPayment);
    }

    @Override
    public ResponseEntity<Void> deleteApplication(Integer applicationId) {
        int deleted = dsl.deleteFrom(Application.APPLICATION)
            .where(Application.APPLICATION.ID.eq(applicationId))
            .execute();

        return deleted > 0
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ApplicationRecord> getApplication(Integer applicationId) {
        var result = dsl.select()
            .from(Application.APPLICATION)
            .where(Application.APPLICATION.ID.eq(applicationId)).fetchOneInto(ApplicationRecord.class);

        return result != null
                ? ResponseEntity.ok(result)
                : ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<List<ApplicationRecord>> listApplications(UUID userId, ApplicationStatusEnum applicationStatus) {
        return null;
    }

    @Override
    public ResponseEntity<List<ApplicationDocumentRecord>> listDocuments(Integer applicationId) {
        return null;
    }

    @Override
    public ResponseEntity<List<ApplicationPaymentRecord>> listPayments(Integer applicationId) {
        return null;
    }

    @Override
    public ResponseEntity<RunLottery200Response> runLottery(RunLotteryRequest runLotteryRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationRecord> updateApplication(Integer applicationId, ApplicationUpdate applicationUpdate) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationPaymentRecord> updatePayment(Integer applicationId, Integer paymentId, UpdatePaymentRequest updatePaymentRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationDocumentRecord> uploadDocument(Integer applicationId, MultipartFile file, String documentType) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationDocumentRecord> verifyDocument(Integer applicationId, Integer documentId, VerifyDocumentRequest verifyDocumentRequest) {
        return null;
    }
}
