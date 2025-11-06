package io.bootify.license_application_service.service;

import io.bootify.license_application_service.domain.AppUser;
import io.bootify.license_application_service.domain.Application;
import io.bootify.license_application_service.domain.License;
import io.bootify.license_application_service.events.BeforeDeleteAppUser;
import io.bootify.license_application_service.events.BeforeDeleteApplication;
import io.bootify.license_application_service.model.LicenseDTO;
import io.bootify.license_application_service.repos.AppUserRepository;
import io.bootify.license_application_service.repos.ApplicationRepository;
import io.bootify.license_application_service.repos.LicenseRepository;
import io.bootify.license_application_service.util.NotFoundException;
import io.bootify.license_application_service.util.ReferencedException;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationRepository applicationRepository;

    public LicenseService(final LicenseRepository licenseRepository,
            final AppUserRepository appUserRepository,
            final ApplicationRepository applicationRepository) {
        this.licenseRepository = licenseRepository;
        this.appUserRepository = appUserRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<LicenseDTO> findAll() {
        final List<License> licenses = licenseRepository.findAll(Sort.by("id"));
        return licenses.stream()
                .map(license -> mapToDTO(license, new LicenseDTO()))
                .toList();
    }

    public LicenseDTO get(final Integer id) {
        return licenseRepository.findById(id)
                .map(license -> mapToDTO(license, new LicenseDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Integer create(final LicenseDTO licenseDTO) {
        final License license = new License();
        mapToEntity(licenseDTO, license);
        return licenseRepository.save(license).getId();
    }

    public void update(final Integer id, final LicenseDTO licenseDTO) {
        final License license = licenseRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(licenseDTO, license);
        licenseRepository.save(license);
    }

    public void delete(final Integer id) {
        final License license = licenseRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        licenseRepository.delete(license);
    }

    private LicenseDTO mapToDTO(final License license, final LicenseDTO licenseDTO) {
        licenseDTO.setId(license.getId());
        licenseDTO.setLicenseType(license.getLicenseType());
        licenseDTO.setLicenseStatus(license.getLicenseStatus());
        licenseDTO.setIssuedAt(license.getIssuedAt());
        licenseDTO.setExpiresAt(license.getExpiresAt());
        licenseDTO.setUser(license.getUser() == null ? null : license.getUser().getId());
        licenseDTO.setApplication(license.getApplication() == null ? null : license.getApplication().getId());
        return licenseDTO;
    }

    private License mapToEntity(final LicenseDTO licenseDTO, final License license) {
        license.setLicenseType(licenseDTO.getLicenseType());
        license.setLicenseStatus(licenseDTO.getLicenseStatus());
        license.setIssuedAt(licenseDTO.getIssuedAt());
        license.setExpiresAt(licenseDTO.getExpiresAt());
        final AppUser user = licenseDTO.getUser() == null ? null : appUserRepository.findById(licenseDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        license.setUser(user);
        final Application application = licenseDTO.getApplication() == null ? null : applicationRepository.findById(licenseDTO.getApplication())
                .orElseThrow(() -> new NotFoundException("application not found"));
        license.setApplication(application);
        return license;
    }

    @EventListener(BeforeDeleteAppUser.class)
    public void on(final BeforeDeleteAppUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final License userLicense = licenseRepository.findFirstByUserId(event.getId());
        if (userLicense != null) {
            referencedException.setKey("appUser.license.user.referenced");
            referencedException.addParam(userLicense.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteApplication.class)
    public void on(final BeforeDeleteApplication event) {
        final ReferencedException referencedException = new ReferencedException();
        final License applicationLicense = licenseRepository.findFirstByApplicationId(event.getId());
        if (applicationLicense != null) {
            referencedException.setKey("application.license.application.referenced");
            referencedException.addParam(applicationLicense.getId());
            throw referencedException;
        }
    }

}
