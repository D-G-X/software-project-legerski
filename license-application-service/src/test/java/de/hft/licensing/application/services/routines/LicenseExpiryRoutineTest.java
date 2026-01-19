package de.hft.licensing.application.services.routines;

import de.hft.licensing.application.repository.LicenseExpiryRoutineDslService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class LicenseExpiryRoutineTest {

    @Test
    void expireLicenses_delegatesToDslService() {
        LicenseExpiryRoutineDslService dsl = mock(LicenseExpiryRoutineDslService.class);
        when(dsl.expireLicenses()).thenReturn(3);

        LicenseExpiryRoutine routine = new LicenseExpiryRoutine(dsl);
        routine.expireLicenses();

        verify(dsl).expireLicenses();
        verifyNoMoreInteractions(dsl);
    }
}