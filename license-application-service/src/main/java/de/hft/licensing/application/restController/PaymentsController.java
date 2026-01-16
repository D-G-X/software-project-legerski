package de.hft.licensing.application.restController;

import de.hft.licensing.api.PaymentsApi;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.ApplicationFeeResource;
import de.hft.licensing.model.ApplicationPaymentCreate;
import de.hft.licensing.model.ApplicationPaymentResource;
import de.hft.licensing.application.services.PaymentService;
import de.hft.licensing.utils.ApiFormValidator;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
public class PaymentsController implements PaymentsApi {

  private static final Logger log = LicensingLoggerFactory.getLogger(PaymentsController.class);

  private final int ETV_amount = 3500;
  private final int ETVPL_amount = 875;
  private final int ETV60_amount = 290;

  private final PaymentService paymentService;
  private final ApiFormValidator formValidator;

  public PaymentsController(PaymentService paymentService, ApiFormValidator formValidator) {
    this.paymentService = paymentService;
    this.formValidator = formValidator;
  }

  @Override
  @PreAuthorize("@paymentAuthorization.canAccessPayments(authentication, #applicationId)")
  public ResponseEntity<ApplicationPaymentResource> createPayment(Integer applicationId, ApplicationPaymentCreate applicationPaymentCreate) {
    if (applicationId == null || applicationPaymentCreate == null || applicationPaymentCreate.getApplicationId() == null) {
      log.error("Application ID is null");
      return ResponseEntity.badRequest().build();
    }

    if (applicationPaymentCreate.getName() != null &&
            applicationPaymentCreate.getIban() != null &&
            applicationPaymentCreate.getBic() != null &&
            !formValidator.isValidName(applicationPaymentCreate.getName()) &&
            !formValidator.isValidIban(applicationPaymentCreate.getIban()) &&
            !formValidator.isValidBic(applicationPaymentCreate.getBic())
    ) {
      log.error("Validation failed for payment creation");
      return ResponseEntity.badRequest().build();
    }

    ApplicationPaymentRecord dbPayment = paymentService.createPayment(applicationId, applicationPaymentCreate);
    if (dbPayment == null) {
      return ResponseEntity.status(500).build();
    }

    ApplicationPaymentResource apiPayment = new ApplicationPaymentResource();
    RecordToResourceMapperUtil.mapApplicationPaymentRecordToResource(dbPayment, apiPayment);

    return ResponseEntity.created(URI.create("/applications/" + applicationId + "/payments/" + apiPayment.getId()))
            .body(apiPayment);
  }


  @Override
  @PreAuthorize("@paymentAuthorization.canAccessPayments(authentication, #applicationId)")
  public ResponseEntity<List<ApplicationPaymentResource>> listPayments(Integer applicationId) {
    if (applicationId == null) {
      return ResponseEntity.badRequest().build();
    }

    List<ApplicationPaymentRecord> payments = paymentService.listPayments(applicationId);
    if (payments == null) {
      return ResponseEntity.notFound().build();
    }

    List<ApplicationPaymentResource> mappedPayments = payments.stream().map(record -> {
      ApplicationPaymentResource resource = new ApplicationPaymentResource();
      RecordToResourceMapperUtil.mapApplicationPaymentRecordToResource(record, resource);
      return resource;
    }).toList();

    return ResponseEntity.ok(mappedPayments);
  }

  @Override
  public ResponseEntity<ApplicationFeeResource> getApplicationFee(Integer applicationId) {
    if (applicationId == null) {
      return ResponseEntity.badRequest().build();
    }

    ApplicationRecord appRecord = paymentService.getApplication(applicationId);
    if (appRecord == null) {
      return ResponseEntity.notFound().build();
    }

    ApplicationFeeResource feeResource = new ApplicationFeeResource();
    feeResource.setApplicationId(applicationId);

    LicenseType applicationLicenseType = appRecord.getLicenseType();
    switch (applicationLicenseType) {
      case etv -> feeResource.setFeeAmount(new BigDecimal(ETV_amount));
      case etvpl -> feeResource.setFeeAmount(new BigDecimal(ETVPL_amount));
      case etv60 -> feeResource.setFeeAmount(new BigDecimal(ETV60_amount));
      default -> feeResource.setFeeAmount(BigDecimal.ZERO);
    }

    return ResponseEntity.ok(feeResource);
  }
}