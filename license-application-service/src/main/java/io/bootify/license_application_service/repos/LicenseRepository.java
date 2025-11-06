package io.bootify.license_application_service.repos;

import io.bootify.license_application_service.domain.License;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LicenseRepository extends JpaRepository<License, Integer> {

    License findFirstByUserId(UUID id);

    License findFirstByApplicationId(Integer id);

}
