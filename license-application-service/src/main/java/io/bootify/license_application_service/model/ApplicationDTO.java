package io.bootify.license_application_service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;


public class ApplicationDTO {

    private Integer id;

    @NotNull
    @Size(max = 255)
    private String licenseType;

    @Size(max = 20)
    private String cadastralReference;

    private OffsetDateTime appliedAt;

    private OffsetDateTime changedAt;

    @Size(max = 255)
    private String status;

    @Size(max = 255)
    private String paymentStatus;

    private UUID user;

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

    public String getCadastralReference() {
        return cadastralReference;
    }

    public void setCadastralReference(final String cadastralReference) {
        this.cadastralReference = cadastralReference;
    }

    public OffsetDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(final OffsetDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(final OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(final String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public UUID getUser() {
        return user;
    }

    public void setUser(final UUID user) {
        this.user = user;
    }

}
