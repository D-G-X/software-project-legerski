package io.bootify.license_application_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
public class Application {

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

    @Column(length = 20)
    private String cadastralReference;

    @Column
    private OffsetDateTime appliedAt;

    @Column
    private OffsetDateTime changedAt;

    @Column
    private String status;

    @Column
    private String paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @OneToMany(mappedBy = "application")
    private Set<License> applicationLicenses = new HashSet<>();

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

    public AppUser getUser() {
        return user;
    }

    public void setUser(final AppUser user) {
        this.user = user;
    }

    public Set<License> getApplicationLicenses() {
        return applicationLicenses;
    }

    public void setApplicationLicenses(final Set<License> applicationLicenses) {
        this.applicationLicenses = applicationLicenses;
    }

}
