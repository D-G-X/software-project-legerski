package de.hft.licensing.application.restController;

import de.hft.licensing.api.ApplicationsApi;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.ApplicationCreate;
import de.hft.licensing.model.ApplicationResource;
import de.hft.licensing.model.ApplicationStatusApiEnum;
import de.hft.licensing.model.ApplicationUpdate;
import de.hft.licensing.application.services.ApplicationService;
import de.hft.licensing.utils.ApiFormValidator;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class ApplicationsController implements ApplicationsApi {

  private static final Logger log = LicensingLoggerFactory.getLogger(ApplicationsController.class);

  private final ApplicationService applicationService;
  private final ApiFormValidator formValidator = new ApiFormValidator();

  public ApplicationsController(ApplicationService applicationService, Validator validator) {
    this.applicationService = applicationService;
  }

  @Override
  @PreAuthorize("@applicationAuthorization.canCreateApplication(authentication, #applicationCreate.userId)")
  public ResponseEntity<ApplicationResource> createApplication(ApplicationCreate applicationCreate) {
    if (applicationCreate == null || applicationCreate.getUserId() == null || applicationCreate.getLicenseType() == null) {
      return ResponseEntity.badRequest().build();
    }

    if (applicationCreate.getCadastralReference() != null &&
            !formValidator.isValidCadastralNumber(applicationCreate.getCadastralReference())) {
      return ResponseEntity.badRequest().build();
    }

    var result = applicationService.createApplication(
            applicationCreate.getUserId(),
            applicationCreate.getLicenseType(),
            applicationCreate.getCadastralReference(),
            applicationCreate.getRemarks()
    );

    if (result.code() == ApplicationService.CreateApplicationResultCode.BALLOT_PERIOD_INACTIVE) {
      return ResponseEntity.status(409).build();
    }
    if (result.code() == ApplicationService.CreateApplicationResultCode.USER_NOT_FOUND) {
      return ResponseEntity.status(422).build();
    }
    if (result.code() == ApplicationService.CreateApplicationResultCode.INTERNAL_ERROR || result.record() == null) {
      return ResponseEntity.status(500).build();
    }

    ApplicationResource apiResource = new ApplicationResource();
    RecordToResourceMapperUtil.mapApplicationRecordToResource(result.record(), apiResource);

    log.info("Created new application with ID {} for user ID {}", apiResource.getId(), apiResource.getUserId());

    return ResponseEntity.created(URI.create("/applications/" + apiResource.getId()))
            .body(apiResource);
  }

  @Override
  @PreAuthorize("@applicationAuthorization.canAccessApplication(authentication, #applicationId)")
  public ResponseEntity<Void> deleteApplication(Integer applicationId) {
    if (applicationId == null) {
      return ResponseEntity.badRequest().build();
    }

    boolean deleted = applicationService.deleteApplication(applicationId);

    if (deleted) {
      log.info("Deleted application with ID {}", applicationId);
      return ResponseEntity.noContent().build();
    } else {
      log.warn("Attempted to delete non-existing application with ID {}", applicationId);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  @PreAuthorize("@applicationAuthorization.canAccessApplication(authentication, #applicationId)")
  public ResponseEntity<ApplicationResource> getApplication(Integer applicationId) {
    if (applicationId == null) {
      return ResponseEntity.badRequest().build();
    }

    ApplicationRecord result = applicationService.getApplication(applicationId);
    if (result == null) {
      return ResponseEntity.notFound().build();
    }

    ApplicationResource apiResource = new ApplicationResource();
    RecordToResourceMapperUtil.mapApplicationRecordToResource(result, apiResource);
    return ResponseEntity.ok(apiResource);
  }

  @Override
  @PreAuthorize("@applicationAuthorization.canListApplications(authentication, #userId)")
  public ResponseEntity<List<ApplicationResource>> listApplications(UUID userId, ApplicationStatusApiEnum applicationStatus) {
    List<ApplicationRecord> result = applicationService.listApplications(userId, applicationStatus);

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
    if (applicationId == null || applicationUpdate == null) {
      return ResponseEntity.badRequest().build();
    }

    if (applicationUpdate.getApplicationStatus() == null
            && applicationUpdate.getRemarks() == null
            && applicationUpdate.getCadastralReference() == null
            && applicationUpdate.getLicenseType() == null) {
      return ResponseEntity.badRequest().build();
    }

    var result = applicationService.updateApplication(applicationId, applicationUpdate);

    if (result.code() == ApplicationService.UpdateApplicationResultCode.NOT_FOUND) {
      log.error("Attempted to update non-existing application with ID {}", applicationId);
      return ResponseEntity.notFound().build();
    }
    if (result.code() == ApplicationService.UpdateApplicationResultCode.INTERNAL_ERROR || result.record() == null) {
      return ResponseEntity.status(500).build();
    }

    ApplicationResource updatedApplicationResource = new ApplicationResource();
    RecordToResourceMapperUtil.mapApplicationRecordToResource(result.record(), updatedApplicationResource);

    return ResponseEntity.ok(updatedApplicationResource);
  }
}