package io.bootify.license_application_service.repos;

import io.bootify.license_application_service.domain.AppUser;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
}
