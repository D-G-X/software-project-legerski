package de.hft.licensing.scheduler;

import de.hft.licensing.db.tables.NotificationPreferences;
import de.hft.licensing.db.tables.PasswordResetToken;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.model.RegisterRequest;
import de.hft.licensing.services.KeycloakAuthService;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ManageStartup {

    private DSLContext dsl;
    private KeycloakAuthService authService;

    /*
    * Admin username will be adminadminadmin@xx.xx
     */
    private static final String ADMIN_NAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_EMAIL = "admin@xx.xx";
    private static final String ADMIN_ROLE = "admin";

    /*
     * Admin username will be adminadminadmin@xx.xx
     */
    private static final String VALIDATOR_FIRSTNAME = "document";
    private static final String VALIDATOR_LASTNAME = "validator";
    private static final String VALIDATOR_PASSWORD = "securepassword123";
    private static final String VALIDATOR_EMAIL = "documentvalidator@xx.xx";
    private static final String VALIDATOR_ROLE = "mock_validator";

    public ManageStartup(KeycloakAuthService authService, DSLContext dsl) {
        this.dsl = dsl;
        this.authService = authService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setupInitialUserInKeycloakAndDatabase() {

        // cleanupUsersNotInKeycloak(); -> because of gdpr reasons, but what to delete and how to keep relations in case of keeping data?

        authService.register(new RegisterRequest()
                .firstname(ADMIN_NAME)
                .lastname(ADMIN_NAME)
                .password(ADMIN_PASSWORD)
                .email(ADMIN_EMAIL)
        );
        authService.setRole(ADMIN_EMAIL, ADMIN_ROLE);

        authService.register(new RegisterRequest()
                .firstname(VALIDATOR_FIRSTNAME)
                .lastname(VALIDATOR_LASTNAME)
                .password(VALIDATOR_PASSWORD)
                .email(VALIDATOR_EMAIL)
        );
        authService.setRole(VALIDATOR_EMAIL, VALIDATOR_ROLE);
    }

    private void cleanupUsersNotInKeycloak() {
        List<UUID> localUserIds =
                dsl.select(User.USER.ID)
                        .from(User.USER)
                        .fetch(User.USER.ID)          // -> List<String>
                        .stream()
                        .map(UUID::fromString)
                        .toList();

        if (localUserIds.isEmpty()) return;

        final List<UUID> orphanIds;
        try {
            orphanIds = localUserIds.stream()
                    .filter(id -> !authService.userExistsInKeycloak(id))
                    .toList();
        } catch (Exception e) {
            System.out.println("[WARN] Keycloak check failed, skipping orphan cleanup: " + e.getMessage());
            return;
        }

        if (orphanIds.isEmpty()) return;

        dsl.transaction(cfg -> {
            var tx = DSL.using(cfg);

            tx.deleteFrom(PasswordResetToken.PASSWORD_RESET_TOKEN)
                    .where(PasswordResetToken.PASSWORD_RESET_TOKEN.USER_ID.in(orphanIds.stream().map(UUID::toString).toList()))
                    .execute();

            tx.deleteFrom(NotificationPreferences.NOTIFICATION_PREFERENCES)
                    .where(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID.in(orphanIds.stream().map(UUID::toString).toList()))
                    .execute();

            tx.deleteFrom(User.USER)
                    .where(User.USER.ID.in(orphanIds.stream().map(UUID::toString).toList()))
                    .execute();
        });

        System.out.println("[INFO] Deleted orphan users from DB: " + orphanIds.size());
    }

}
