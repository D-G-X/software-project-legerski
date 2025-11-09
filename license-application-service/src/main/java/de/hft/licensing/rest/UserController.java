package de.hft.licensing.rest;

import de.hft.licensing.api.UsersApi;

import de.hft.licensing.model.CreateUserRequest;
import de.hft.licensing.model.UpdateUserRequest;
import de.hft.licensing.model.UserRecord;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID userId) {
        return null;
    }

    @Override
    public ResponseEntity<UserRecord> getUser(UUID userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserRecord>> listUsers(String username, String email, Integer first, Integer max) {
        return null;
    }

    @Override
    public ResponseEntity<Void> updateUser(String userId, UpdateUserRequest updateUserRequest) {
        return null;
    }
}
