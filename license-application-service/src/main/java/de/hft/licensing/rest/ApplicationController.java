package de.hft.licensing.rest;

import de.hft.licensing.api.ApplicationsApi;
import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.ApplicationPayment;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.*;
import de.hft.licensing.utils.EnumMapperUtil;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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
                .fetchOneInto(ApplicationRecord.class);

        if (dbRecord == null) {
            return ResponseEntity.status(500).build();
        }

        // map DB record -> API model and convert enums explicitly
        ApplicationResource apiResource = new ApplicationResource();
        RecordToResourceMapperUtil.mapApplicationRecordToResource(dbRecord, apiResource);

        return ResponseEntity.created(URI.create("/applications/" + apiResource.getId())).body(apiResource);
    }


    @Override
    public ResponseEntity<ApplicationPaymentResource> createPayment(Integer applicationId, ApplicationPaymentCreate applicationPaymentCreate) {
        if (applicationId == null || applicationPaymentCreate.getApplicationId() == null) {
            return ResponseEntity.badRequest().build();
        }
        LocalDateTime now = LocalDateTime.now();
        var dbPayment = dsl.insertInto(ApplicationPayment.APPLICATION_PAYMENT)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_DATE, now)
                .set(ApplicationPayment.APPLICATION_PAYMENT.AMOUNT, applicationPaymentCreate.getAmount())
                .set(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID, applicationId)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS, (PaymentStatus) EnumMapperUtil.getPendantFromEnum(applicationPaymentCreate.getPaymentStatus()))
                .returning()
                .fetchOneInto(ApplicationPaymentRecord.class);

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
            .where(Application.APPLICATION.ID.eq(applicationId))
            .fetchOneInto(ApplicationRecord.class);

        ApplicationResource apiResource = new ApplicationResource();
        RecordToResourceMapperUtil.mapApplicationRecordToResource(result, apiResource);

        return result != null
                ? ResponseEntity.ok(apiResource)
                : ResponseEntity.notFound().build();
    }

    //TODO: finish implementation as Parameter Types clash
    @Override
    public ResponseEntity<List<ApplicationResource>> listApplications(UUID userId, ApplicationStatusApiEnum applicationStatus) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Maps roles from JWT token
        boolean isAdmin = jwt.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
        // Extract the user ID from Keycloak token: "sub" claim
        UUID currentUserId = UUID.fromString(jwt.getToken().getSubject());
        // Enforce: normal users can only see their own applications
        if (!isAdmin) {
            userId = currentUserId;
        }

        List<ApplicationRecord> result = null;
        // no filters
        if(userId == null && applicationStatus == null) {
            result = dsl.select()
                    .from(Application.APPLICATION)
                    .fetchInto(ApplicationRecord.class);
        }
        // filter by userId only
        else if (userId != null && applicationStatus == null) {
            result = dsl.select()
                    .from(Application.APPLICATION)
                    .where(Application.APPLICATION.USER_ID.eq(userId.toString()))
                    .fetchInto(ApplicationRecord.class);
        }
        // filter by applicationStatus only
        else if (userId == null) {
            result = dsl.select()
                    .from(Application.APPLICATION)
                    .where(Application.APPLICATION.APPLICATION_STATUS.eq((ApplicationStatus) EnumMapperUtil.getPendantFromEnum(applicationStatus)))
                    .fetchInto(ApplicationRecord.class);
        }
        // filter by both userId and applicationStatus
        else {
            result = dsl.select()
                    .from(Application.APPLICATION)
                    .where(Application.APPLICATION.USER_ID.eq(userId.toString())
                        .and(Application.APPLICATION.APPLICATION_STATUS.eq((ApplicationStatus) EnumMapperUtil.getPendantFromEnum(applicationStatus))))
                    .fetchInto(ApplicationRecord.class);
        }
        List<ApplicationResource> mappedResult = result.stream().map(record -> {
            ApplicationResource resource = new ApplicationResource();
            RecordToResourceMapperUtil.mapApplicationRecordToResource(record, resource);
            return resource;
        }).toList();
        return ResponseEntity.ok(mappedResult);
    }

    @Override
    public ResponseEntity<List<ApplicationDocumentResource>> listDocuments(Integer applicationId) {
        return null;
    }

    @Override
    public ResponseEntity<List<ApplicationPaymentResource>> listPayments(Integer applicationId) {
        if (applicationId == null) {
            return ResponseEntity.badRequest().build();
        }
        if(!dsl.fetchExists(
                dsl.selectOne()
                        .from(Application.APPLICATION)
                        .where(Application.APPLICATION.ID.eq(applicationId))
        )) {
            return ResponseEntity.notFound().build();
        }
        var payments = dsl.select()
                .from(ApplicationPayment.APPLICATION_PAYMENT)
                .where(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID.eq(applicationId))
                .fetchInto(ApplicationPaymentRecord.class);

        List<ApplicationPaymentResource> mappedPayments = payments.stream().map(record -> {
            ApplicationPaymentResource resource = new ApplicationPaymentResource();
            RecordToResourceMapperUtil.mapApplicationPaymentRecordToResource(record, resource);
            return resource;
        }).toList();

        return ResponseEntity.ok(mappedPayments);
    }

    // TODO: Implement lottery logic
    @Override
    public ResponseEntity<RunLottery200Response> runLottery(RunLotteryRequest runLotteryRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationResource> updateApplication(Integer applicationId, ApplicationUpdate applicationUpdate) {
        if (applicationId == null || applicationUpdate == null || applicationUpdate.getApplicationStatus() == null || applicationUpdate.getRemarks() == null) {
            return ResponseEntity.badRequest().build();
        }
        var updatedApplicationRecord = dsl.update(Application.APPLICATION)
                .set(Application.APPLICATION.APPLICATION_STATUS, (ApplicationStatus) EnumMapperUtil.getPendantFromEnum(applicationUpdate.getApplicationStatus()))
                .set(Application.APPLICATION.REMARKS, applicationUpdate.getRemarks())
                .set(Application.APPLICATION.CHANGED_AT, LocalDateTime.now())
                .where(Application.APPLICATION.ID.eq(applicationId))
                .returning()
                .fetchOneInto(ApplicationRecord.class);

        if(updatedApplicationRecord != null){
            ApplicationResource updatedApplicationResource = new ApplicationResource();
            RecordToResourceMapperUtil.mapApplicationRecordToResource(updatedApplicationRecord, updatedApplicationResource);
            return ResponseEntity.ok(updatedApplicationResource);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ApplicationPaymentResource> updatePayment(Integer applicationId, Integer paymentId, UpdatePaymentRequest updatePaymentRequest) {
        if(applicationId == null || paymentId == null || updatePaymentRequest == null || updatePaymentRequest.getPaymentStatus() == null) {
            return ResponseEntity.badRequest().build();
        }
        var updatedPaymentRecord = dsl.update(ApplicationPayment.APPLICATION_PAYMENT)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS, (PaymentStatus) EnumMapperUtil.getPendantFromEnum(updatePaymentRequest.getPaymentStatus()))
                .where(ApplicationPayment.APPLICATION_PAYMENT.ID.eq(paymentId))
                .and(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID.eq(applicationId))
                .returning()
                .fetchOneInto(ApplicationPaymentRecord.class);

        if(updatedPaymentRecord != null){
            ApplicationPaymentResource updatedPaymentResource = new ApplicationPaymentResource();
            RecordToResourceMapperUtil.mapApplicationPaymentRecordToResource(updatedPaymentRecord, updatedPaymentResource);
            return ResponseEntity.ok(updatedPaymentResource);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ApplicationDocumentResource> uploadDocument(Integer applicationId, MultipartFile file, String documentType) {
        return null;
    }

    // TODO: Send Api call to Document Service to verify document
    @Override
    public ResponseEntity<ApplicationDocumentResource> verifyDocument(Integer applicationId, Integer documentId, VerifyDocumentRequest verifyDocumentRequest) {
        return null;
    }
}
