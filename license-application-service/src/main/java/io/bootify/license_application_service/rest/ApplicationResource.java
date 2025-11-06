package io.bootify.license_application_service.rest;

import io.bootify.license_application_service.model.ApplicationDTO;
import io.bootify.license_application_service.service.AppUserService;
import io.bootify.license_application_service.service.ApplicationService;
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
@RequestMapping(value = "/api/applications", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApplicationResource {

    private final ApplicationService applicationService;
    private final AppUserService appUserService;

    public ApplicationResource(final ApplicationService applicationService,
            final AppUserService appUserService) {
        this.applicationService = applicationService;
        this.appUserService = appUserService;
    }

    @GetMapping
    public ResponseEntity<List<ApplicationDTO>> getAllApplications() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDTO> getApplication(
            @PathVariable(name = "id") final Integer id) {
        return ResponseEntity.ok(applicationService.get(id));
    }

    @PostMapping
    public ResponseEntity<Integer> createApplication(
            @RequestBody @Valid final ApplicationDTO applicationDTO) {
        final Integer createdId = applicationService.create(applicationDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Integer> updateApplication(@PathVariable(name = "id") final Integer id,
            @RequestBody @Valid final ApplicationDTO applicationDTO) {
        applicationService.update(id, applicationDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable(name = "id") final Integer id) {
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/userValues")
    public ResponseEntity<Map<UUID, UUID>> getUserValues() {
        return ResponseEntity.ok(appUserService.getAppUserValues());
    }

}
