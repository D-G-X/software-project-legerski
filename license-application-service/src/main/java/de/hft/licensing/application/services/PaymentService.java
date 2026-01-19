package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.ApplicationDslService;
import de.hft.licensing.application.repository.DistributionAlgorithmDslService;
import de.hft.licensing.application.repository.PaymentDslService;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.ApplicationPaymentCreate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private static final int ETV_amount = 3500;
    private static final int ETVPL_amount = 875;
    private static final int ETV60_amount = 290;

    private final PaymentDslService repository;
    private final DistributionAlgorithmDslService distributionAlgorithmDslService;
    private final ApplicationDslService applicationDslService;

    public PaymentService(PaymentDslService repository,
                          DistributionAlgorithmDslService distributionAlgorithmDslService, ApplicationDslService applicationDslService) {
        this.repository = repository;
        this.distributionAlgorithmDslService = distributionAlgorithmDslService;
        this.applicationDslService = applicationDslService;
    }

    @Transactional
    public ApplicationPaymentRecord createPayment(Integer applicationId, ApplicationPaymentCreate create) {
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());

        return repository.createPayment(
                applicationId,
                now,
                calculateFeeAmount(applicationId),
                create.getName(),
                create.getIban(),
                create.getBic(),
                PaymentStatus.unpaid,
                distributionAlgorithmDslService.getCurrentBallotPeriodId()
        );
    }

    @Transactional(readOnly = true)
    public List<ApplicationPaymentRecord> listPayments(Integer applicationId) {
        if (!repository.applicationExists(applicationId)) {
            return null;
        }
        return repository.listPayments(applicationId);
    }

    @Transactional(readOnly = true)
    public ApplicationRecord getApplication(Integer applicationId) {
        return repository.getApplication(applicationId);
    }

    /**
     * Calculates the fee amount based on the application ID.
     *
     * @param applicationId the ID of the application
     * @return the calculated fee amount
     */
    public BigDecimal calculateFeeAmount(Integer applicationId) {
        ApplicationRecord appRecord = applicationDslService.getApplication(applicationId);

        LicenseType applicationLicenseType = appRecord.getLicenseType();
        switch (applicationLicenseType) {
            case etv -> {
                return new BigDecimal(ETV_amount);
            }
            case etvpl -> {
                return new BigDecimal(ETVPL_amount);
            }
            case etv60 -> {
                return new BigDecimal(ETV60_amount);
            }
            default -> {
                return BigDecimal.ZERO;
            }
        }
    }
}