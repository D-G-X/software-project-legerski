package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.ApplicationPayment;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentDslServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DSLContext dsl;

    private PaymentDslService service;

    @BeforeEach
    void setUp() {
        service = new PaymentDslService(dsl);
    }

    @Test
    void applicationExists_true() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(true);
        assertTrue(service.applicationExists(1));
    }

    @Test
    void applicationExists_false() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(false);
        assertFalse(service.applicationExists(1));
    }

    @Test
    void getApplication_returnsRecord() {
        ApplicationRecord rec = new ApplicationRecord();
        when(dsl.selectFrom(Application.APPLICATION)
                .where((Condition) any())
                .fetchOneInto(ApplicationRecord.class)).thenReturn(rec);

        assertSame(rec, service.getApplication(1));
    }

    @Test
    void listPayments_returnsList() {
        List<ApplicationPaymentRecord> list = List.of(new ApplicationPaymentRecord());
        when(dsl.selectFrom(ApplicationPayment.APPLICATION_PAYMENT)
                .where((Condition) any())
                .fetchInto(ApplicationPaymentRecord.class)).thenReturn(list);

        assertSame(list, service.listPayments(1));
    }

    @Test
    void createPayment_returnsRecord() {
        ApplicationPaymentRecord rec = new ApplicationPaymentRecord();

        when(dsl.insertInto(ApplicationPayment.APPLICATION_PAYMENT)
                .set(eq(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_DATE), (LocalDateTime) any())
                .set(eq(ApplicationPayment.APPLICATION_PAYMENT.AMOUNT), eq(BigDecimal.TEN))
                .set(eq(ApplicationPayment.APPLICATION_PAYMENT.ACCOUNTANT), eq("acc"))
                .set(eq(ApplicationPayment.APPLICATION_PAYMENT.IBAN), eq("iban"))
                .set(eq(ApplicationPayment.APPLICATION_PAYMENT.BIC), eq("bic"))
                .set(eq(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID), eq(1))
                .set(eq(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS), eq(PaymentStatus.unpaid))
                .set(eq(ApplicationPayment.APPLICATION_PAYMENT.BALLOT_PERIOD_ID), eq(7))
                .returning()
                .fetchOneInto(ApplicationPaymentRecord.class)).thenReturn(rec);

        assertSame(rec, service.createPayment(
                1, LocalDateTime.now(), BigDecimal.TEN, "acc", "iban", "bic", PaymentStatus.unpaid, 7
        ));
    }
}