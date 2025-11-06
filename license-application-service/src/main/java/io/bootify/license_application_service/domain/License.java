package io.bootify.license_application_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import java.time.OffsetDateTime;


@Entity
public class License {

    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1,
            initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Integer id;

    @Column(nullable = false)
    private String licenseType;

    @Column
    private String licenseStatus;

    @Column
    private OffsetDateTime issuedAt;

    @Column
    private OffsetDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(final String licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseStatus() {
        return licenseStatus;
    }

    public void setLicenseStatus(final String licenseStatus) {
        this.licenseStatus = licenseStatus;
    }

    public OffsetDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(final OffsetDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(final AppUser user) {
        this.user = user;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(final Application application) {
        this.application = application;
    }

}
