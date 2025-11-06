package io.bootify.license_application_service.repos;

import io.bootify.license_application_service.domain.Application;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    Application findFirstByUserId(UUID id);

}
