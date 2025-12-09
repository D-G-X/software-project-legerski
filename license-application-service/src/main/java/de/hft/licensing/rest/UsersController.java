package de.hft.licensing.rest;

import de.hft.licensing.api.UsersApi;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.UserRecord;
import de.hft.licensing.model.CreateUserRequest;
import de.hft.licensing.model.UpdateUserRequest;
import de.hft.licensing.model.UserResource;
import de.hft.licensing.services.auth.AdminOnly;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.DSLContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class UsersController implements UsersApi {

    private final DSLContext dsl;

    public UsersController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @AdminOnly
    public ResponseEntity<Void> createUser(CreateUserRequest createUserRequest) {
        if (createUserRequest == null || createUserRequest.getSchema() == null || createUserRequest.getSchema().getUsername() == null) {
            return ResponseEntity.badRequest().build();
        }

        // no keycloal integration yet, just create a local user id
        String id = UUID.randomUUID().toString();

        try {
            int inserted = dsl.insertInto(User.USER)
                    .set(User.USER.ID, id)
                    .execute();

            if (inserted > 0) {
                return ResponseEntity.created(URI.create("/users/" + id)).build();
            } else {
                return ResponseEntity.status(500).build();
            }
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @Override
    @AdminOnly
    public ResponseEntity<Void> deleteUser(UUID userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        int deleted = dsl.deleteFrom(User.USER)
                .where(User.USER.ID.eq(userId.toString()))
                .execute();

        return deleted > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Override
    @AdminOnly
    public ResponseEntity<UserResource> getUser(UUID userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean exists = dsl.fetchExists(
                dsl.selectOne().from(User.USER).where(User.USER.ID.eq(userId.toString()))
        );

        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        // only id column, other attributes are managed by Keycloak??
        UserResource user = new UserResource();
        user.setId(userId);
        return ResponseEntity.ok(user);
    }

    @Override
    @AdminOnly
    public ResponseEntity<List<UserResource>> listUsers(String username, String email, Integer first, Integer max) {
        // only column is id, so ignore filters for now
        int offset = (first == null || first < 0) ? 0 : first;
        int limit = (max == null || max <= 0) ? 100 : Math.min(max, 100);

        var idsList = dsl.select()
                .from(User.USER)
                .offset(offset)
                .limit(limit)
                .fetchInto(UserRecord.class);

        List<UserResource> result = idsList.stream().map(record -> {
            UserResource user = new UserResource();
            RecordToResourceMapperUtil.mapUserRecordToResource(record, user);
            return user;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @Override
    @AdminOnly
    public ResponseEntity<Void> updateUser(String userId, UpdateUserRequest updateUserRequest) {
        if (userId == null || updateUserRequest == null) {
            return ResponseEntity.badRequest().build();
        }

        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        boolean exists = dsl.fetchExists(
                dsl.selectOne().from(User.USER).where(User.USER.ID.eq(id.toString()))
        );

        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        // No local columns to update in this schema; user attributes are managed by Keycloak.
        // integrate with Keycloak, perform that call here
        return ResponseEntity.noContent().build();
    }
}
