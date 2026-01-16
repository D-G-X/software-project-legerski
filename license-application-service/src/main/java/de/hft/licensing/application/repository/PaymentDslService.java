package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.ApplicationPayment;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentDslService {

    private final DSLContext dsl;

    public PaymentDslService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean applicationExists(Integer applicationId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(Application.APPLICATION)
                        .where(Application.APPLICATION.ID.eq(applicationId))
        );
    }

    public ApplicationRecord getApplication(Integer applicationId) {
        return dsl.selectFrom(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOneInto(ApplicationRecord.class);
    }

    public List<ApplicationPaymentRecord> listPayments(Integer applicationId) {
        return dsl.selectFrom(ApplicationPayment.APPLICATION_PAYMENT)
                .where(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID.eq(applicationId))
                .fetchInto(ApplicationPaymentRecord.class);
    }

    public ApplicationPaymentRecord createPayment(
            Integer applicationId,
            LocalDateTime paymentDate,
            BigDecimal amount,
            String accountant,
            String iban,
            String bic,
            PaymentStatus status,
            Integer ballotPeriodId
    ) {
        return dsl.insertInto(ApplicationPayment.APPLICATION_PAYMENT)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_DATE, paymentDate)
                .set(ApplicationPayment.APPLICATION_PAYMENT.AMOUNT, amount)
                .set(ApplicationPayment.APPLICATION_PAYMENT.ACCOUNTANT, accountant)
                .set(ApplicationPayment.APPLICATION_PAYMENT.IBAN, iban)
                .set(ApplicationPayment.APPLICATION_PAYMENT.BIC, bic)
                .set(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID, applicationId)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS, status)
                .set(ApplicationPayment.APPLICATION_PAYMENT.BALLOT_PERIOD_ID, ballotPeriodId)
                .returning()
                .fetchOneInto(ApplicationPaymentRecord.class);
    }
}