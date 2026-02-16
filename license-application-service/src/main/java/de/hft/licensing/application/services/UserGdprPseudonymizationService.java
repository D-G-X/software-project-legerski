package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.UserDslService;
import de.hft.licensing.logger.LicensingLoggerFactory;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.Table;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Service
public class UserGdprPseudonymizationService {

    private final UserDslService dsl;
    private static final Logger log = LicensingLoggerFactory.getLogger(UserGdprPseudonymizationService.class);

    @Value("${gdpr.pepper}")
    private byte[] pepper;

    public UserGdprPseudonymizationService(UserDslService userDslService) {
        this.dsl = userDslService;
        if(pepper == null || pepper.length == 0) {
            log.error("GDPR pepper is not configured properly!");
        }
    }

    /**
     * Pseudonymizes a user ID across all tables in the database.
     *
     * @param oldUserId the original user ID to be pseudonymized
     * @return the new pseudonymized user ID, or null if an error occurred
     */
    @Transactional
    public String pseudonymizeUserIdEverywhere(UUID oldUserId) {
        if (oldUserId == null) {
            log.error("oldUserId must not be null");
            return null;
        }

        final String oldId = oldUserId.toString();

        boolean exists = dsl.userExists(oldId);
        if (!exists) {
            log.error("User not found in local DB: {}", oldId);
            return null;
        }

        // generate new pseudonymized ID
        String newId = hmacToUuidString(oldId);

        // collision check for rare cases
        if (dsl.userExists(newId)) {
            log.warn("Collision detected when pseudonymizing user id: {}", oldId);
            newId = hmacToUuidString(oldId + "#collision");
            if (newId != null || dsl.userExists(newId)) {
                log.error("Could not generate unique pseudonymized user id");
                return null;
            }
        }

        dsl.createUser(newId);

        var TABLE_SCHEMA = field(name("table_schema"), String.class);
        var TABLE_NAME   = field(name("table_name"), String.class);
        var UDT_NAME     = field(name("udt_name"), String.class);

        Result<Record3<String, String, String>> userIdColumns = dsl.getAllUserIdCellsInDb(TABLE_SCHEMA, TABLE_NAME, UDT_NAME);

        for (Record3<String, String, String> r : userIdColumns) {
            String schema = r.value1();
            String table  = r.value2();
            String udt    = r.value3();

            Table<?> t = table(name(schema, table));

            if ("uuid".equalsIgnoreCase(udt)) {
                Field<UUID> col = field(name(schema, table, "user_id"), UUID.class);
                dsl.updateTableFieldsWithNewValue(t, col, UUID.fromString(oldId), UUID.fromString(newId));
            } else {
                Field<String> col = field(name(schema, table, "user_id"), String.class);
                dsl.updateTableFieldsWithNewValue(t, col, oldId, newId);
            }
        }

        int deleted = dsl.deleteUser(oldId);

        if (deleted != 1) {
            log.error("Failed to delete old user record for id: {}", oldId);
            return null;
        }
        log.info("Pseudonymized user");
        return newId;
    }

    /**
     * Generates a pseudonymized UUID string using HMAC-SHA256.
     *
     * @param oldId the original user ID
     * @return the pseudonymized UUID string
     */
    private String hmacToUuidString(String oldId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pepper, "HmacSHA256"));
            byte[] digest = mac.doFinal(oldId.getBytes(StandardCharsets.UTF_8));

            byte[] uuidBytes = new byte[16];
            System.arraycopy(digest, 0, uuidBytes, 0, 16);

            uuidBytes[6] = (byte) ((uuidBytes[6] & 0x0F) | 0x40);
            uuidBytes[8] = (byte) ((uuidBytes[8] & 0x3F) | 0x80);

            long msb = ByteBuffer.wrap(uuidBytes, 0, 8).getLong();
            long lsb = ByteBuffer.wrap(uuidBytes, 8, 8).getLong();
            return new UUID(msb, lsb).toString();
        } catch (Exception e) {
            log.error("Failed to derive pseudonymized UUID", e);
            return null;
        }
    }
}