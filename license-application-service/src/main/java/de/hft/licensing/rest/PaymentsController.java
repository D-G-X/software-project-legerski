package de.hft.licensing.rest;

import de.hft.licensing.api.PaymentsApi;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.ApplicationPayment;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.ApplicationFeeResource;
import de.hft.licensing.model.ApplicationPaymentCreate;
import de.hft.licensing.model.ApplicationPaymentResource;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class PaymentsController implements PaymentsApi {

    private final DSLContext dsl;

    public PaymentsController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @PreAuthorize("@paymentAuthorization.canAccessPayments(authentication, #applicationId)")
    @Transactional
    public ResponseEntity<ApplicationPaymentResource> createPayment(Integer applicationId, ApplicationPaymentCreate applicationPaymentCreate) {
        if (applicationId == null || applicationPaymentCreate.getApplicationId() == null) {
            return ResponseEntity.badRequest().build();
        }
        LocalDateTime now = LocalDateTime.now();
        var dbPayment = dsl.insertInto(ApplicationPayment.APPLICATION_PAYMENT)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_DATE, now)
                .set(ApplicationPayment.APPLICATION_PAYMENT.AMOUNT, new BigDecimal("99.99")) //TODO: add amount to model and DB
                .set(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID, applicationId)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS, PaymentStatus.unpaid)
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
    @PreAuthorize("@paymentAuthorization.canAccessPayments(authentication, #applicationId)")
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

    @Override
    public ResponseEntity<ApplicationFeeResource> getApplicationFee(Integer applicationId) {
        final int ETV_amount = 3500;
        final int ETVPL_amount = 875;
        final int ETV60_amount = 290;

        if (applicationId == null) {
            return ResponseEntity.badRequest().build();
        }

        ApplicationRecord appRecord = dsl.select()
                .from(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOneInto(ApplicationRecord.class);
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
