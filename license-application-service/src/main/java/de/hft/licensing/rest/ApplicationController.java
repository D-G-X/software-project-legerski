package de.hft.licensing.rest;

import de.hft.licensing.api.ApplicationsApi;
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
import org.jooq.DSLContext;
import de.hft.licensing.db.tables.Application;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
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
                        .from(de.hft.licensing.db.tables.User.USER)
                        .where(de.hft.licensing.db.tables.User.USER.ID.eq(applicationCreate.getUserId().toString()))
        );
        if (!userExists) {
            // client provided a user_id that does not exist
            return ResponseEntity.status(422).build();
        }

        LocalDateTime now = LocalDateTime.now();

        de.hft.licensing.db.enums.LicenseType dbLicenseType;
        try {
            dbLicenseType = de.hft.licensing.db.enums.LicenseType.valueOf(applicationCreate.getLicenseType().name().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            // fallback: try direct name or literal lookup (some generated enums use different naming)
            try {
                dbLicenseType = de.hft.licensing.db.enums.LicenseType.valueOf(applicationCreate.getLicenseType().name());
            } catch (IllegalArgumentException ignored2) {
                dbLicenseType = de.hft.licensing.db.enums.LicenseType.lookupLiteral(applicationCreate.getLicenseType().toString());
            }
        }
        if (dbLicenseType == null) {
            return ResponseEntity.badRequest().build();
        }

        var created = dsl.insertInto(Application.APPLICATION)
                .set(Application.APPLICATION.USER_ID, applicationCreate.getUserId().toString())
                .set(Application.APPLICATION.APPLICATION_STATUS, de.hft.licensing.db.enums.ApplicationStatus.draft)
                .set(Application.APPLICATION.APPLIED_AT, now)
                .set(Application.APPLICATION.CHANGED_AT, now)
                .set(Application.APPLICATION.CADASTRAL_REFERENCE, applicationCreate.getCadastralReference())
                .set(Application.APPLICATION.LICENSE_TYPE, dbLicenseType)
                .set(Application.APPLICATION.REMARKS, applicationCreate.getRemarks())
                .returning()
                .fetchOneInto(ApplicationRecord.class);

        return created != null
                ? ResponseEntity.ok(created)
                : ResponseEntity.status(500).build();
    }

    @Override
    public ResponseEntity<ApplicationPaymentRecord> createPayment(Integer applicationId, ApplicationPaymentCreate applicationPaymentCreate) {
        return null;
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
    public ResponseEntity<List<ApplicationRecord>> listApplications(UUID userId, de.hft.licensing.model.ApplicationStatus applicationStatus) {
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
