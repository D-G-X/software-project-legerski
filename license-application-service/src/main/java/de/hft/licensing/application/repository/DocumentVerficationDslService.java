package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.VerificationStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

@Service
public class DocumentVerficationDslService {

    private final DSLContext dsl;

    public DocumentVerficationDslService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public int updateVerificationStatus(Integer applicationId, VerificationStatus status) {
        return dsl.update(Application.APPLICATION)
                .set(Application.APPLICATION.VERIFICATION_STATUS, status)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .execute();
    }

    public int updateRemarks(Integer applicationId, String remarks) {
        return dsl.update(Application.APPLICATION)
                .set(Application.APPLICATION.REMARKS, remarks)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .execute();
    }

    public ApplicationRecord getApplication(Integer applicationId) {
        return dsl.selectFrom(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOneInto(ApplicationRecord.class);
    }
}