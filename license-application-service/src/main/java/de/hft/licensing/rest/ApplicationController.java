package de.hft.licensing.rest;

import de.hft.licensing.api.ApplicationsApi;
import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.ApplicationPayment;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.model.*;
import de.hft.licensing.utils.EnumMapperUtil;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
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
    public ResponseEntity<ApplicationResource> createApplication(ApplicationCreate applicationCreate) {
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

        // insert and return DB record (jooq DB record, not API model record)
        var dbRecord = dsl.insertInto(Application.APPLICATION)
                .set(Application.APPLICATION.USER_ID, applicationCreate.getUserId().toString())
                .set(Application.APPLICATION.APPLICATION_STATUS, ApplicationStatus.draft)
                .set(Application.APPLICATION.APPLIED_AT, now)
                .set(Application.APPLICATION.CHANGED_AT, now)
                .set(Application.APPLICATION.CADASTRAL_REFERENCE, applicationCreate.getCadastralReference())
                .set(Application.APPLICATION.LICENSE_TYPE, (LicenseType) EnumMapperUtil.getPendantFromEnum(applicationCreate.getLicenseType()))
                .set(Application.APPLICATION.REMARKS, applicationCreate.getRemarks())
                .returning()
                .fetchOne();

        if (dbRecord == null) {
            return ResponseEntity.status(500).build();
        }

        // map DB record -> API model and convert enums explicitly
        ApplicationResource api = new ApplicationResource();
        RecordToResourceMapperUtil.mapApplicationRecordToResource(dbRecord, api);

        return ResponseEntity.created(URI.create("/applications/" + api.getId())).body(api);
    }


    @Override
    public ResponseEntity<ApplicationPaymentResource> createPayment(Integer applicationId, ApplicationPaymentCreate applicationPaymentCreate) {
        if (applicationId == null || applicationPaymentCreate.getApplicationId() == null || !Objects.equals(applicationPaymentCreate.getApplicationId(), applicationId)) {
            return ResponseEntity.badRequest().build();
        }
        LocalDateTime now = LocalDateTime.now();
        var dbPayment = dsl.insertInto(ApplicationPayment.APPLICATION_PAYMENT)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_DATE, now)
                .set(ApplicationPayment.APPLICATION_PAYMENT.AMOUNT, applicationPaymentCreate.getAmount())
                .set(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID, applicationPaymentCreate.getApplicationId())
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS, (PaymentStatus) EnumMapperUtil.getPendantFromEnum(applicationPaymentCreate.getPaymentStatus()))
                .returning()
                .fetchOne();
        if(dbPayment == null) {
            return ResponseEntity.status(500).build();
        }

        ApplicationPaymentResource apiPayment = new ApplicationPaymentResource();
        RecordToResourceMapperUtil.mapApplicationPaymentRecordToResource(dbPayment, apiPayment);

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
    public ResponseEntity<ApplicationResource> getApplication(Integer applicationId) {
        var result = dsl.select()
            .from(Application.APPLICATION)
            .where(Application.APPLICATION.ID.eq(applicationId)).fetchOneInto(ApplicationResource.class);

        return result != null
                ? ResponseEntity.ok(result)
                : ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<List<ApplicationResource>> listApplications(UUID userId, ApplicationStatusEnum applicationStatus) {
        return null;
    }

    @Override
    public ResponseEntity<List<ApplicationDocumentResource>> listDocuments(Integer applicationId) {
        return null;
    }

    @Override
    public ResponseEntity<List<ApplicationPaymentResource>> listPayments(Integer applicationId) {
        return null;
    }

    @Override
    public ResponseEntity<RunLottery200Response> runLottery(RunLotteryRequest runLotteryRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationResource> updateApplication(Integer applicationId, ApplicationUpdate applicationUpdate) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationPaymentResource> updatePayment(Integer applicationId, Integer paymentId, UpdatePaymentRequest updatePaymentRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationDocumentResource> uploadDocument(Integer applicationId, MultipartFile file, String documentType) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationDocumentResource> verifyDocument(Integer applicationId, Integer documentId, VerifyDocumentRequest verifyDocumentRequest) {
        return null;
    }
}
