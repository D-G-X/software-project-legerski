package io.bootify.license_application_service.rest;

import io.bootify.license_application_service.model.LicenseDTO;
import io.bootify.license_application_service.service.AppUserService;
import io.bootify.license_application_service.service.ApplicationService;
import io.bootify.license_application_service.service.LicenseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/licenses", produces = MediaType.APPLICATION_JSON_VALUE)
public class LicenseResource {

    private final LicenseService licenseService;
    private final AppUserService appUserService;
    private final ApplicationService applicationService;

    public LicenseResource(final LicenseService licenseService, final AppUserService appUserService,
            final ApplicationService applicationService) {
        this.licenseService = licenseService;
        this.appUserService = appUserService;
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<List<LicenseDTO>> getAllLicenses() {
        return ResponseEntity.ok(licenseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LicenseDTO> getLicense(@PathVariable(name = "id") final Integer id) {
        return ResponseEntity.ok(licenseService.get(id));
    }

    @PostMapping
    public ResponseEntity<Integer> createLicense(@RequestBody @Valid final LicenseDTO licenseDTO) {
        final Integer createdId = licenseService.create(licenseDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Integer> updateLicense(@PathVariable(name = "id") final Integer id,
            @RequestBody @Valid final LicenseDTO licenseDTO) {
        licenseService.update(id, licenseDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLicense(@PathVariable(name = "id") final Integer id) {
        licenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/userValues")
    public ResponseEntity<Map<UUID, UUID>> getUserValues() {
        return ResponseEntity.ok(appUserService.getAppUserValues());
    }

    @GetMapping("/applicationValues")
    public ResponseEntity<Map<Integer, String>> getApplicationValues() {
        return ResponseEntity.ok(applicationService.getApplicationValues());
    }

}
