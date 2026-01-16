package de.hft.licensing.services.dslService;

import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.tables.Application;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.stereotype.Service;

@Service
public class UserDslService {

    private final DefaultDSLContext dsl;

    private final ApplicationStatus CANCELLED = ApplicationStatus.cancelled;

    public UserDslService(DefaultDSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Sets the application status to 'cancelled' for a given user ID.
     *
     * @param userId the ID of the user whose application status is to be updated
     * @return the number of records updated
     */
    public int setApplicationStatusToCancelled(String userId) {
        return dsl.update(de.hft.licensing.db.tables.Application.APPLICATION)
           .set(Application.APPLICATION.APPLICATION_STATUS, CANCELLED)
           .where(String.valueOf(Application.APPLICATION.USER_ID), userId)
           .execute();
    }
}
