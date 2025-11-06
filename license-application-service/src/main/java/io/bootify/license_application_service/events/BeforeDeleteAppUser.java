package io.bootify.license_application_service.events;

import java.util.UUID;


public class BeforeDeleteAppUser {

    private UUID id;

    public BeforeDeleteAppUser(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

}
