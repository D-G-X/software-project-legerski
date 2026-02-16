package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.ApplicationPayment;
import de.hft.licensing.db.tables.Ballot;
import de.hft.licensing.db.tables.BallotPeriod;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.selectOne;

@Service
public class BallotPeriodDslService {

    private final DSLContext dsl;

    public BallotPeriodDslService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<BallotPeriodRecord> listBallotPeriods() {
        return dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .orderBy(BallotPeriod.BALLOT_PERIOD.ID.desc())
                .fetchInto(BallotPeriodRecord.class);
    }

    public int countApplicationsInPeriod(Integer periodId) {
        return dsl.selectCount()
                .from(Ballot.BALLOT)
                .where(Ballot.BALLOT.BALLOT_PERIOD_ID.eq(periodId))
                .fetchOneInto(int.class);
    }

    public boolean overlaps(LocalDateTime newStart, LocalDateTime newEnd) {
        return dsl.fetchExists(
                selectOne()
                        .from(BallotPeriod.BALLOT_PERIOD)
                        .where(BallotPeriod.BALLOT_PERIOD.START_DATE.le(newEnd))
                        .and(BallotPeriod.BALLOT_PERIOD.END_DATE.ge(newStart))
        );
    }

    public BallotPeriodRecord createBallotPeriod(LocalDateTime newStart, LocalDateTime newEnd) {
        return dsl.insertInto(BallotPeriod.BALLOT_PERIOD)
                .set(BallotPeriod.BALLOT_PERIOD.START_DATE, newStart)
                .set(BallotPeriod.BALLOT_PERIOD.END_DATE, newEnd)
                .returning()
                .fetchOneInto(BallotPeriodRecord.class);
    }

    public BallotPeriodRecord getActiveBallotPeriod(LocalDateTime nowUtc) {
        return dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where(BallotPeriod.BALLOT_PERIOD.START_DATE.le(nowUtc))
                .and(BallotPeriod.BALLOT_PERIOD.END_DATE.ge(nowUtc))
                .fetchOneInto(BallotPeriodRecord.class);
    }

    public BallotPeriodRecord getBallotPeriodById(Integer periodId) {
        return dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where(BallotPeriod.BALLOT_PERIOD.ID.eq(periodId))
                .fetchOneInto(BallotPeriodRecord.class);
    }

    public List<ApplicationRecord> getBallotPeriodEntries(Integer periodId) {
        return dsl.selectFrom(Application.APPLICATION)
                .whereExists(
                        dsl.selectOne()
                                .from(Ballot.BALLOT)
                                .where(Ballot.BALLOT.APPLICATION_ID.eq(Application.APPLICATION.ID))
                                .and(Ballot.BALLOT.BALLOT_PERIOD_ID.eq(periodId))
                )
                .fetchInto(ApplicationRecord.class);
    }

    public int markSelected(Integer periodId, Integer applicationId) {
        return dsl.update(Ballot.BALLOT)
                .set(Ballot.BALLOT.SELECTED, true)
                .where(Ballot.BALLOT.APPLICATION_ID.eq(applicationId))
                .and(Ballot.BALLOT.BALLOT_PERIOD_ID.eq(periodId))
                .execute();
    }

    public ApplicationPaymentRecord getPaymentByApplicationId(Integer appId) {
        return dsl.selectFrom(ApplicationPayment.APPLICATION_PAYMENT)
                .where(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID.eq(appId))
                .fetchOneInto(ApplicationPaymentRecord.class);
    }

    public int updatePaymentStatus(Integer appId, PaymentStatus status) {
        return dsl.update(ApplicationPayment.APPLICATION_PAYMENT)
                .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS, status)
                .where(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID.eq(appId))
                .execute();
    }

    public int truncateApplicationPayments() {
        return dsl.truncate(ApplicationPayment.APPLICATION_PAYMENT).execute();
    }
}