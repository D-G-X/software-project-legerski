package de.hft.licensing.application.restController;

import de.hft.licensing.api.ConsentApi;
import de.hft.licensing.model.CheckConsentForUser200Response;
import de.hft.licensing.model.CheckConsentForUserRequest;
import de.hft.licensing.model.ConsentCreate;
import de.hft.licensing.model.ConsentResource;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ConsentController implements ConsentApi {

    private final DSLContext dsl;

    public ConsentController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ResponseEntity<CheckConsentForUser200Response> checkConsentForUser(CheckConsentForUserRequest checkConsentForUserRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConsentResource> createConsentForUser(ConsentCreate consentCreate) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteConsentForUser(String consentId) {
        return null;
    }

    @Override
    public ResponseEntity<List<ConsentResource>> listConsentsForUser() {
        return null;
    }

    @Override
    public ResponseEntity<Void> requestGdprDeletion(UUID userId) {
        return null;
    }
}
