package de.hft.licensing.services;

import de.hft.licensing.db.tables.User;
import de.hft.licensing.logger.LicensingLoggerFactory;
import org.jooq.*;
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

    private final DSLContext dsl;
    private static final Logger log = LicensingLoggerFactory.getLogger(UserGdprPseudonymizationService.class);

    @Value("${gdpr.pepper}")
    private byte[] pepper;

    public UserGdprPseudonymizationService(DSLContext dsl) {
        this.dsl = dsl;
        if(pepper == null || pepper.length == 0) {
            log.error("GDPR pepper is not configured properly!");
        }
    }

    @Transactional
    public String pseudonymizeUserIdEverywhere(UUID oldUserId) {
        if (oldUserId == null) {
            log.error("oldUserId must not be null");
            return null;
        }

        final String oldId = oldUserId.toString();

        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(User.USER)
                        .where(User.USER.ID.eq(oldId))
                        .forUpdate()
        );
        if (!exists) {
            log.error("User not found in local DB: {}", oldId);
            return null;
        }

        // generate new pseudonymized ID
        String newId = hmacToUuidString(oldId);

        // collision check for rare cases
        if (dsl.fetchExists(dsl.selectOne().from(User.USER).where(User.USER.ID.eq(newId)))) {
            newId = hmacToUuidString(oldId + "#collision");
            if (newId != null || dsl.fetchExists(dsl.selectOne().from(User.USER).where(User.USER.ID.eq(newId)))) {
                log.error("Could not generate unique pseudonymized user id");
                return null;
            }
        }

        dsl.insertInto(User.USER)
                .set(User.USER.ID, newId)
                .execute();

        var TABLE_SCHEMA = field(name("table_schema"), String.class);
        var TABLE_NAME   = field(name("table_name"), String.class);
        var UDT_NAME     = field(name("udt_name"), String.class);

        Result<Record3<String, String, String>> userIdColumns =
                dsl.select(TABLE_SCHEMA, TABLE_NAME, UDT_NAME)
                        .from(table(name("information_schema", "columns")))
                        .where(field(name("column_name"), String.class).eq("user_id"))
                        .and(field(name("table_schema"), String.class).notIn("pg_catalog", "information_schema"))
                        .and(not(TABLE_SCHEMA.eq("public").and(TABLE_NAME.eq("user"))))
                        .fetch();

        for (Record3<String, String, String> r : userIdColumns) {
            String schema = r.value1();
            String table  = r.value2();
            String udt    = r.value3();

            Table<?> t = table(name(schema, table));

            if ("uuid".equalsIgnoreCase(udt)) {
                Field<UUID> col = field(name(schema, table, "user_id"), UUID.class);
                dsl.update(t)
                        .set(col, UUID.fromString(newId))
                        .where(col.eq(oldUserId))
                        .execute();
            } else {
                Field<String> col = field(name(schema, table, "user_id"), String.class);
                dsl.update(t)
                        .set(col, newId)
                        .where(col.eq(oldId))
                        .execute();
            }
        }

        int deleted = dsl.deleteFrom(User.USER)
                .where(User.USER.ID.eq(oldId))
                .execute();

        if (deleted != 1) {
            log.error("Failed to delete old user record for id: {}", oldId);
            return null;
        }
        log.info("Pseudonymized user");
        return newId;
    }


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