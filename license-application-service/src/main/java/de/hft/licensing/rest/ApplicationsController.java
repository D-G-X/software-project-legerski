package de.hft.licensing.rest;

import de.hft.licensing.api.ApplicationsApi;
import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.VerificationStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.Ballot;
import de.hft.licensing.db.tables.BallotPeriod;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.ApplicationCreate;
import de.hft.licensing.model.ApplicationResource;
import de.hft.licensing.model.ApplicationStatusApiEnum;
import de.hft.licensing.model.ApplicationUpdate;
import de.hft.licensing.utils.ApiFormValidator;
import de.hft.licensing.utils.EnumMapperUtil;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class ApplicationsController implements ApplicationsApi {

  private final DSLContext dsl;
  private static final Logger log = LicensingLoggerFactory.getLogger(ApplicationsController.class);
  private final ApiFormValidator formValidator = new ApiFormValidator();

  public ApplicationsController(DSLContext dsl, Validator validator) {
    this.dsl = dsl;
  }

  @Override
  @PreAuthorize("@applicationAuthorization.canCreateApplication(authentication, #applicationCreate.userId)")
  @Transactional
  public ResponseEntity<ApplicationResource> createApplication(
      ApplicationCreate applicationCreate) {
    if (applicationCreate == null || applicationCreate.getUserId() == null
        || applicationCreate.getLicenseType() == null) {
      log.warn("Invalid application creation request: missing required fields");
      return ResponseEntity.badRequest().build();
    }
    if (applicationCreate.getCadastralReference() != null &&
        !formValidator.isValidCadastralNumber(applicationCreate.getCadastralReference())) {
        log.warn("Invalid application creation request: invalid cadastral reference format");
      return ResponseEntity.badRequest().build();
    }
    LocalDateTime nowUtc = LocalDateTime.now(Clock.systemUTC());
    
    boolean ballotPeriodActive = dsl.fetchExists(
        dsl.selectOne()
            .from(BallotPeriod.BALLOT_PERIOD)
            .where(BallotPeriod.BALLOT_PERIOD.START_DATE.le(nowUtc))
            .and(BallotPeriod.BALLOT_PERIOD.END_DATE.ge(nowUtc))
    );

    if (!ballotPeriodActive) {
      log.warn("Attempted to create application outside of active ballot period");
      return ResponseEntity.status(409).build();
    }

    boolean userExists = dsl.fetchExists(
        dsl.selectOne()
            .from(User.USER)
            .where(User.USER.ID.eq(applicationCreate.getUserId().toString()))
    );
    if (!userExists) {
        log.warn("Attempted to create application for non-existing user ID {}", applicationCreate.getUserId());
      return ResponseEntity.status(422).build();
    }

    // insert and return DB record (jooq DB record, not API model record)
    var dbRecord = dsl.insertInto(Application.APPLICATION)
        .set(Application.APPLICATION.USER_ID, applicationCreate.getUserId().toString())
        .set(Application.APPLICATION.APPLICATION_STATUS, ApplicationStatus.draft)
        .set(Application.APPLICATION.APPLIED_AT, nowUtc)
        .set(Application.APPLICATION.CHANGED_AT, nowUtc)
        .set(Application.APPLICATION.CADASTRAL_REFERENCE, applicationCreate.getCadastralReference())
        .set(Application.APPLICATION.LICENSE_TYPE,
            (LicenseType) EnumMapperUtil.getPendantFromEnum(applicationCreate.getLicenseType()))
        .set(Application.APPLICATION.REMARKS, applicationCreate.getRemarks())
        .set(Application.APPLICATION.VERIFICATION_STATUS, VerificationStatus.pending)
        .returning()
        .fetchOneInto(ApplicationRecord.class);

    if (dbRecord == null) {
        log.error("Error while creating application for user ID {}", applicationCreate.getUserId());
      return ResponseEntity.status(500).build();
    }

    try {
      Integer currentBallotPeriodId = dsl.select(BallotPeriod.BALLOT_PERIOD.ID)
              .from(BallotPeriod.BALLOT_PERIOD)
              .where(BallotPeriod.BALLOT_PERIOD.START_DATE.le(nowUtc))
              .and(BallotPeriod.BALLOT_PERIOD.END_DATE.ge(nowUtc))
              .fetchOneInto(Integer.class);

      dsl.insertInto(Ballot.BALLOT)
              .set(Ballot.BALLOT.BALLOT_PERIOD_ID, currentBallotPeriodId)
              .set(Ballot.BALLOT.APPLICATION_ID, dbRecord.getId())
              .set(Ballot.BALLOT.SELECTED, false)
              .execute();

    } catch (Exception e) {
      log.error("Error while creating ballot entry for application ID {}: {}", dbRecord.getId(),
          e.getMessage());
      return ResponseEntity.status(500).build();
    }


    // map DB record -> API model and convert enums explicitly
    ApplicationResource apiResource = new ApplicationResource();
    RecordToResourceMapperUtil.mapApplicationRecordToResource(dbRecord, apiResource);

    // Logger
    log.info("Created new application with ID {} for user ID {}", apiResource.getId(),
        apiResource.getUserId());

    return ResponseEntity.created(URI.create("/applications/" + apiResource.getId()))
        .body(apiResource);
  }

  @Override
  @PreAuthorize("@applicationAuthorization.canAccessApplication(authentication, #applicationId)")
  @Transactional
  public ResponseEntity<Void> deleteApplication(Integer applicationId) {
    int deleted = dsl.deleteFrom(Application.APPLICATION)
        .where(Application.APPLICATION.ID.eq(applicationId))
        .execute();

    // Logger
    if (deleted > 0) {
      log.info("Deleted application with ID {}", applicationId);
    } else {
      log.warn("Attempted to delete non-existing application with ID {}", applicationId);
    }

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
  public ResponseEntity<List<ApplicationResource>> listApplications(UUID userId,
      ApplicationStatusApiEnum applicationStatus) {
    List<ApplicationRecord> result;
    // no filters
    if (userId == null && applicationStatus == null) {
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
          .where(Application.APPLICATION.APPLICATION_STATUS.eq(
              (ApplicationStatus) EnumMapperUtil.getPendantFromEnum(applicationStatus)))
          .fetchInto(ApplicationRecord.class);
    }
    // filter by both userId and applicationStatus
    else {
      result = dsl.select()
          .from(Application.APPLICATION)
          .where(Application.APPLICATION.USER_ID.eq(userId.toString())
              .and(Application.APPLICATION.APPLICATION_STATUS.eq(
                  (ApplicationStatus) EnumMapperUtil.getPendantFromEnum(applicationStatus))))
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
  @Transactional
  public ResponseEntity<ApplicationResource> updateApplication(Integer applicationId,
      ApplicationUpdate applicationUpdate) {
    if (applicationId == null || applicationUpdate == null) {
      return ResponseEntity.badRequest().build();
    }

    // if no fields provided to update, return bad request
    if (applicationUpdate.getApplicationStatus() == null
        && applicationUpdate.getRemarks() == null
        && applicationUpdate.getCadastralReference() == null
        && applicationUpdate.getLicenseType() == null) {
      return ResponseEntity.badRequest().build();
    }
    
    LocalDateTime nowUtc = LocalDateTime.now(Clock.systemUTC());

    var oldStatus = dsl.select(Application.APPLICATION.APPLICATION_STATUS)
        .from(Application.APPLICATION)
        .where(Application.APPLICATION.ID.eq(applicationId))
        .fetchOneInto(ApplicationStatus.class);
    var oldRemarks = dsl.select(Application.APPLICATION.REMARKS)
        .from(Application.APPLICATION)
        .where(Application.APPLICATION.ID.eq(applicationId))
        .fetchOneInto(String.class);

    Map<org.jooq.Field<?>, Object> updates = new HashMap<>();
    if (applicationUpdate.getApplicationStatus() != null) {
      updates.put(Application.APPLICATION.APPLICATION_STATUS,EnumMapperUtil.getPendantFromEnum(applicationUpdate.getApplicationStatus()));
    }
    if (applicationUpdate.getRemarks() != null) {
      updates.put(Application.APPLICATION.REMARKS, applicationUpdate.getRemarks());
    }
    if (applicationUpdate.getCadastralReference() != null) {
      updates.put(Application.APPLICATION.CADASTRAL_REFERENCE, applicationUpdate.getCadastralReference());
    }
    if (applicationUpdate.getLicenseType() != null) {
      updates.put(Application.APPLICATION.LICENSE_TYPE,EnumMapperUtil.getPendantFromEnum(applicationUpdate.getLicenseType()));
    }
    updates.put(Application.APPLICATION.CHANGED_AT, nowUtc);

    var updatedApplicationRecord = dsl.update(Application.APPLICATION)
        .set(updates)
        .where(Application.APPLICATION.ID.eq(applicationId))
        .returning()
        .fetchOneInto(ApplicationRecord.class);

    if (updatedApplicationRecord != null) {
      ApplicationResource updatedApplicationResource = new ApplicationResource();
      RecordToResourceMapperUtil.mapApplicationRecordToResource(updatedApplicationRecord,
          updatedApplicationResource);

      var logs = getString(updatedApplicationRecord, oldStatus, oldRemarks);
      log.info(logs);
      return ResponseEntity.ok(updatedApplicationResource);
    }
    log.error("Attempted to update non-existing application with ID {}", applicationId);
    return ResponseEntity.notFound().build();
  }

  /**
   * Generates log string for updated application record.
   *
   * @param updatedApplicationRecord The updated application record.
   * @param oldStatus                The old application status.
   * @param oldRemarks               The old remarks.
   * @return The log string.
   */
  private static String getString(ApplicationRecord updatedApplicationRecord, ApplicationStatus oldStatus, String oldRemarks) {
    var logs = String.format("Updated application with ID %d:", updatedApplicationRecord.getId());
    if (oldStatus != updatedApplicationRecord.getApplicationStatus()) {
      var oldStatusName = oldStatus != null ? oldStatus.name() : "null";
      logs += String.format(" status updated from %s to %s;", oldStatusName,
          updatedApplicationRecord.getApplicationStatus().name());
    }
    if (!Objects.equals(oldRemarks, updatedApplicationRecord.getRemarks())) {
      logs += String.format(" remarks updated from '%s' to '%s';", oldRemarks,
          updatedApplicationRecord.getRemarks());
    }
    return logs;
  }

}
