package io.bootify.license_application_service.events;


public class BeforeDeleteApplication {

    private Integer id;

    public BeforeDeleteApplication(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

}
