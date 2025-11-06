package io.bootify.license_application_service.service;

import io.bootify.license_application_service.domain.AppUser;
import io.bootify.license_application_service.events.BeforeDeleteAppUser;
import io.bootify.license_application_service.model.AppUserDTO;
import io.bootify.license_application_service.repos.AppUserRepository;
import io.bootify.license_application_service.util.CustomCollectors;
import io.bootify.license_application_service.util.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher publisher;

    public AppUserService(final AppUserRepository appUserRepository,
            final ApplicationEventPublisher publisher) {
        this.appUserRepository = appUserRepository;
        this.publisher = publisher;
    }

    public List<AppUserDTO> findAll() {
        final List<AppUser> appUsers = appUserRepository.findAll(Sort.by("id"));
        return appUsers.stream()
                .map(appUser -> mapToDTO(appUser, new AppUserDTO()))
                .toList();
    }

    public AppUserDTO get(final UUID id) {
        return appUserRepository.findById(id)
                .map(appUser -> mapToDTO(appUser, new AppUserDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final AppUserDTO appUserDTO) {
        final AppUser appUser = new AppUser();
        mapToEntity(appUserDTO, appUser);
        return appUserRepository.save(appUser).getId();
    }

    public void update(final UUID id, final AppUserDTO appUserDTO) {
        final AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(appUserDTO, appUser);
        appUserRepository.save(appUser);
    }

    public void delete(final UUID id) {
        final AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteAppUser(id));
        appUserRepository.delete(appUser);
    }

    private AppUserDTO mapToDTO(final AppUser appUser, final AppUserDTO appUserDTO) {
        appUserDTO.setId(appUser.getId());
        return appUserDTO;
    }

    private AppUser mapToEntity(final AppUserDTO appUserDTO, final AppUser appUser) {
        return appUser;
    }

    public Map<UUID, UUID> getAppUserValues() {
        return appUserRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(AppUser::getId, AppUser::getId));
    }

}
