package io.bootify.license_application_service.rest;

import io.bootify.license_application_service.model.AppUserDTO;
import io.bootify.license_application_service.service.AppUserService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping(value = "/api/appUsers", produces = MediaType.APPLICATION_JSON_VALUE)
public class AppUserResource {

    private final AppUserService appUserService;

    public AppUserResource(final AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public ResponseEntity<List<AppUserDTO>> getAllAppUsers() {
        return ResponseEntity.ok(appUserService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserDTO> getAppUser(@PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(appUserService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createAppUser(@RequestBody @Valid final AppUserDTO appUserDTO) {
        final UUID createdId = appUserService.create(appUserDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateAppUser(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final AppUserDTO appUserDTO) {
        appUserService.update(id, appUserDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppUser(@PathVariable(name = "id") final UUID id) {
        appUserService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
