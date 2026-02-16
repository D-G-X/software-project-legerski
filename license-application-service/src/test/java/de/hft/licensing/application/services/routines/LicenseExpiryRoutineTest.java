package de.hft.licensing.application.services.routines;

import de.hft.licensing.application.repository.LicenseExpiryRoutineDslService;
import de.hft.licensing.application.services.ApplicationService;
import de.hft.licensing.application.services.EmailService;
import de.hft.licensing.application.services.UserService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class LicenseExpiryRoutineTest {

    @Test
    void expireLicenses_delegatesToDslService() {
        LicenseExpiryRoutineDslService dsl = mock(LicenseExpiryRoutineDslService.class);
        ApplicationService applicationService = mock(ApplicationService.class);
        UserService userService = mock(UserService.class);
        EmailService emailService = mock(EmailService.class);
        when(dsl.expireLicenses()).thenReturn(3);

        LicenseExpiryRoutine routine = new LicenseExpiryRoutine(dsl, applicationService, userService, emailService);
        routine.expireLicenses();

        verify(dsl).expireLicenses();
        verifyNoMoreInteractions(dsl);
    }
}