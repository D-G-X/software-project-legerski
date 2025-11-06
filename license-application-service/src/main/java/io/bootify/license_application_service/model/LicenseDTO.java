package io.bootify.license_application_service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;


public class LicenseDTO {

    private Integer id;

    @NotNull
    @Size(max = 255)
    private String licenseType;

    @Size(max = 255)
    private String licenseStatus;

    private OffsetDateTime issuedAt;

    private OffsetDateTime expiresAt;

    private UUID user;

    private Integer application;

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

    public UUID getUser() {
        return user;
    }

    public void setUser(final UUID user) {
        this.user = user;
    }

    public Integer getApplication() {
        return application;
    }

    public void setApplication(final Integer application) {
        this.application = application;
    }

}
