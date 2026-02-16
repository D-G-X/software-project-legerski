package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.ApplicationPayment;
import de.hft.licensing.db.tables.Ballot;
import de.hft.licensing.db.tables.BallotPeriod;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import org.jooq.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BallotPeriodDslServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DSLContext dsl;

    private BallotPeriodDslService service;

    @BeforeEach
    void setUp() {
        service = new BallotPeriodDslService(dsl);
    }

    @Test
    @Disabled
    void listBallotPeriods_returnsList() {
        List<BallotPeriodRecord> list = List.of(new BallotPeriodRecord());
        when(dsl.select(BallotPeriod.BALLOT_PERIOD)
                .from(BallotPeriod.BALLOT_PERIOD)
                .leftJoin(Ballot.BALLOT)
                .on((Condition) any())
                .groupBy((GroupField) any())
                .orderBy((OrderField<Object>) any())
                .fetchInto(BallotPeriodRecord.class)).thenReturn(list);

        assertSame(list, service.listBallotPeriods());
    }

    @Test
    void countApplicationsInPeriod_returnsCount() {
        when(dsl.selectCount()
                .from(Ballot.BALLOT)
                .where((Condition) any())
                .fetchOneInto(int.class)).thenReturn(5);

        assertEquals(5, service.countApplicationsInPeriod(7));
    }

    @Test
    void overlaps_true() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(true);
        assertTrue(service.overlaps(LocalDateTime.now(), LocalDateTime.now().plusDays(1)));
    }

    @Test
    void overlaps_false() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(false);
        assertFalse(service.overlaps(LocalDateTime.now(), LocalDateTime.now().plusDays(1)));
    }

    @Test
    void createBallotPeriod_returnsRecord() {
        BallotPeriodRecord rec = new BallotPeriodRecord();
        LocalDateTime s = LocalDateTime.now();
        LocalDateTime e = s.plusDays(1);

        when(dsl.insertInto(BallotPeriod.BALLOT_PERIOD)
                .set(eq(BallotPeriod.BALLOT_PERIOD.START_DATE), eq(s))
                .set(eq(BallotPeriod.BALLOT_PERIOD.END_DATE), eq(e))
                .returning()
                .fetchOneInto(BallotPeriodRecord.class)).thenReturn(rec);

        assertSame(rec, service.createBallotPeriod(s, e));
    }

    @Test
    void getActiveBallotPeriod_returnsRecord() {
        BallotPeriodRecord rec = new BallotPeriodRecord();
        when(dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where((Condition) any())
                .and((Condition) any())
                .fetchOneInto(BallotPeriodRecord.class)).thenReturn(rec);

        assertSame(rec, service.getActiveBallotPeriod(LocalDateTime.now()));
    }

    @Test
    void getBallotPeriodById_returnsRecord() {
        BallotPeriodRecord rec = new BallotPeriodRecord();
        when(dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where((Condition) any())
                .fetchOneInto(BallotPeriodRecord.class)).thenReturn(rec);

        assertSame(rec, service.getBallotPeriodById(1));
    }

    @Test
    void getBallotPeriodEntries_returnsList() {
        List<ApplicationRecord> list = List.of(new ApplicationRecord());

        when(dsl.selectFrom(Application.APPLICATION)
                .whereExists(
                        dsl.selectOne()
                                .from(Ballot.BALLOT)
                                .where((Condition) any())
                                .and((Condition) any())
                )
                .fetchInto(ApplicationRecord.class)).thenReturn(list);

        assertSame(list, service.getBallotPeriodEntries(1));
    }

    @Test
    void markSelected_returnsUpdatedRows() {
        when(dsl.update(Ballot.BALLOT)
                .set(eq(Ballot.BALLOT.SELECTED), eq(true))
                .where((Condition) any())
                .and((Condition) any())
                .execute()).thenReturn(1);

        assertEquals(1, service.markSelected(1, 2));
    }

    @Test
    void getPaymentByApplicationId_returnsRecord() {
        ApplicationPaymentRecord rec = new ApplicationPaymentRecord();

        when(dsl.selectFrom(ApplicationPayment.APPLICATION_PAYMENT)
                .where((Condition) any())
                .fetchOneInto(ApplicationPaymentRecord.class)).thenReturn(rec);

        assertSame(rec, service.getPaymentByApplicationId(3));
    }

    @Test
    void updatePaymentStatus_returnsUpdatedRows() {
        when(dsl.update(ApplicationPayment.APPLICATION_PAYMENT)
                .set(eq(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS), eq(PaymentStatus.paid))
                .where((Condition) any())
                .execute()).thenReturn(1);

        assertEquals(1, service.updatePaymentStatus(3, PaymentStatus.paid));
    }

    @Test
    void truncateApplicationPayments_executes() {
        when(dsl.truncate(ApplicationPayment.APPLICATION_PAYMENT).execute()).thenReturn(1);
        assertEquals(1, service.truncateApplicationPayments());
    }
}