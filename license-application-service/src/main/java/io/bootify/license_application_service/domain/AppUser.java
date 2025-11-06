package io.bootify.license_application_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;


@Entity
public class AppUser {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToMany(mappedBy = "user")
    private Set<Application> userApplications = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<License> userLicenses = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public Set<Application> getUserApplications() {
        return userApplications;
    }

    public void setUserApplications(final Set<Application> userApplications) {
        this.userApplications = userApplications;
    }

    public Set<License> getUserLicenses() {
        return userLicenses;
    }

    public void setUserLicenses(final Set<License> userLicenses) {
        this.userLicenses = userLicenses;
    }

}
