package io.bootify.license_application_service.service;

import io.bootify.license_application_service.domain.AppUser;
import io.bootify.license_application_service.domain.Application;
import io.bootify.license_application_service.events.BeforeDeleteAppUser;
import io.bootify.license_application_service.events.BeforeDeleteApplication;
import io.bootify.license_application_service.model.ApplicationDTO;
import io.bootify.license_application_service.repos.AppUserRepository;
import io.bootify.license_application_service.repos.ApplicationRepository;
import io.bootify.license_application_service.util.CustomCollectors;
import io.bootify.license_application_service.util.NotFoundException;
import io.bootify.license_application_service.util.ReferencedException;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher publisher;

    public ApplicationService(final ApplicationRepository applicationRepository,
            final AppUserRepository appUserRepository, final ApplicationEventPublisher publisher) {
        this.applicationRepository = applicationRepository;
        this.appUserRepository = appUserRepository;
        this.publisher = publisher;
    }

    public List<ApplicationDTO> findAll() {
        final List<Application> applications = applicationRepository.findAll(Sort.by("id"));
        return applications.stream()
                .map(application -> mapToDTO(application, new ApplicationDTO()))
                .toList();
    }

    public ApplicationDTO get(final Integer id) {
        return applicationRepository.findById(id)
                .map(application -> mapToDTO(application, new ApplicationDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Integer create(final ApplicationDTO applicationDTO) {
        final Application application = new Application();
        mapToEntity(applicationDTO, application);
        return applicationRepository.save(application).getId();
    }

    public void update(final Integer id, final ApplicationDTO applicationDTO) {
        final Application application = applicationRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(applicationDTO, application);
        applicationRepository.save(application);
    }

    public void delete(final Integer id) {
        final Application application = applicationRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteApplication(id));
        applicationRepository.delete(application);
    }

    private ApplicationDTO mapToDTO(final Application application,
            final ApplicationDTO applicationDTO) {
        applicationDTO.setId(application.getId());
        applicationDTO.setLicenseType(application.getLicenseType());
        applicationDTO.setCadastralReference(application.getCadastralReference());
        applicationDTO.setAppliedAt(application.getAppliedAt());
        applicationDTO.setChangedAt(application.getChangedAt());
        applicationDTO.setStatus(application.getStatus());
        applicationDTO.setPaymentStatus(application.getPaymentStatus());
        applicationDTO.setUser(application.getUser() == null ? null : application.getUser().getId());
        return applicationDTO;
    }

    private Application mapToEntity(final ApplicationDTO applicationDTO,
            final Application application) {
        application.setLicenseType(applicationDTO.getLicenseType());
        application.setCadastralReference(applicationDTO.getCadastralReference());
        application.setAppliedAt(applicationDTO.getAppliedAt());
        application.setChangedAt(applicationDTO.getChangedAt());
        application.setStatus(applicationDTO.getStatus());
        application.setPaymentStatus(applicationDTO.getPaymentStatus());
        final AppUser user = applicationDTO.getUser() == null ? null : appUserRepository.findById(applicationDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        application.setUser(user);
        return application;
    }

    public Map<Integer, String> getApplicationValues() {
        return applicationRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Application::getId, Application::getLicenseType));
    }

    @EventListener(BeforeDeleteAppUser.class)
    public void on(final BeforeDeleteAppUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final Application userApplication = applicationRepository.findFirstByUserId(event.getId());
        if (userApplication != null) {
            referencedException.setKey("appUser.application.user.referenced");
            referencedException.addParam(userApplication.getId());
            throw referencedException;
        }
    }

}
