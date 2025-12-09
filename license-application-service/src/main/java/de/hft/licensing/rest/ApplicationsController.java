package de.hft.licensing.rest;

import de.hft.licensing.api.ApplicationsApi;
import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.VerificationStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.ApplicationCreate;
import de.hft.licensing.model.ApplicationResource;
import de.hft.licensing.model.ApplicationStatusApiEnum;
import de.hft.licensing.model.ApplicationUpdate;
import de.hft.licensing.utils.EnumMapperUtil;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
public class ApplicationsController implements ApplicationsApi {

    private final DSLContext dsl;

    public ApplicationsController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @PreAuthorize("@applicationAuthorization.canCreateApplication(authentication, #applicationCreate.userId)")
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
                .set(Application.APPLICATION.VERIFICATION_STATUS, VerificationStatus.pending)
                .returning()
                .fetchOneInto(ApplicationRecord.class);

        if (dbRecord == null) {
            return ResponseEntity.status(500).build();
        }

        // TODO: if active ballot period add SUBMITTED to ballotperiod

        // map DB record -> API model and convert enums explicitly
        ApplicationResource apiResource = new ApplicationResource();
        RecordToResourceMapperUtil.mapApplicationRecordToResource(dbRecord, apiResource);

        return ResponseEntity.created(URI.create("/applications/" + apiResource.getId())).body(apiResource);
    }

    @Override
    @PreAuthorize("@applicationAuthorization.canAccessApplication(authentication, #applicationId)")
    public ResponseEntity<Void> deleteApplication(Integer applicationId) {
        int deleted = dsl.deleteFrom(Application.APPLICATION)
            .where(Application.APPLICATION.ID.eq(applicationId))
            .execute();

        return deleted > 0
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Override
    @PreAuthorize("@applicationAuthorization.canAccessApplication(authentication, #applicationId)")
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

    @Override
    @PreAuthorize("@applicationAuthorization.canListApplications(authentication, #userId)")
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
        if (userId != null && !isAdmin && !userId.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (!isAdmin) {
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
    @PreAuthorize("@applicationAuthorization.canAccessApplication(authentication, #applicationId)")
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


        // TODO: if active ballot period add SUBMITTED to ballotperiod

        if(updatedApplicationRecord != null){
            ApplicationResource updatedApplicationResource = new ApplicationResource();
            RecordToResourceMapperUtil.mapApplicationRecordToResource(updatedApplicationRecord, updatedApplicationResource);
            return ResponseEntity.ok(updatedApplicationResource);
        }
        return ResponseEntity.notFound().build();
    }

}