package de.hft.licensing.rest;

import de.hft.licensing.api.UsersApi;

import de.hft.licensing.db.tables.User;
import de.hft.licensing.model.CreateUserRequest;
import de.hft.licensing.model.UpdateUserRequest;
import de.hft.licensing.model.UserResource;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.dao.DataIntegrityViolationException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class UserController implements UsersApi {

    private final DSLContext dsl;

    public UserController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
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
    public ResponseEntity<List<UserResource>> listUsers(String username, String email, Integer first, Integer max) {
        // only column is id, so ignore filters for now
        int offset = (first == null || first < 0) ? 0 : first;
        int limit = (max == null || max <= 0) ? 100 : Math.min(max, 100);

        var ids = dsl.select(User.USER.ID).from(User.USER).offset(offset).limit(limit).fetch(User.USER.ID);

        List<UserResource> result = new ArrayList<>(ids.size());
        for (String idStr : ids) {
            try {
                UUID id = UUID.fromString(idStr);
                UserResource ur = new UserResource();
                ur.setId(id);
                result.add(ur);
            } catch (IllegalArgumentException ignored) {
                // skip bdaly formed ids
            }
        }

        return ResponseEntity.ok(result);
    }

    @Override
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
        // If you want to integrate with Keycloak, perform that call here. Return 204 to indicate success.
        return ResponseEntity.noContent().build();
    }
}
