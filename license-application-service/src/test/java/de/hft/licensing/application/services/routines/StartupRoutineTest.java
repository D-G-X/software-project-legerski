package de.hft.licensing.application.services.routines;

import de.hft.licensing.application.services.AuthService;
import de.hft.licensing.model.RegisterRequest;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartupRoutineTest {

    @Test
    void setupInitialUserInKeycloakAndDatabase_registersAdminAndValidator_andSetsRoles() {
        AuthService authService = mock(AuthService.class);
        DSLContext dsl = mock(DSLContext.class);

        StartupRoutine routine = new StartupRoutine(authService, dsl);
        routine.setupInitialUserInKeycloakAndDatabase();

        ArgumentCaptor<RegisterRequest> regCaptor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService, times(2)).register(regCaptor.capture());

        var first = regCaptor.getAllValues().get(0);
        assertEquals("admin", first.getFirstname());
        assertEquals("admin", first.getLastname());
        assertEquals("admin@xx.xx", first.getEmail());
        assertEquals("admin", first.getPassword());

        var second = regCaptor.getAllValues().get(1);
        assertEquals("document", second.getFirstname());
        assertEquals("validator", second.getLastname());
        assertEquals("documentvalidator@xx.xx", second.getEmail());
        assertEquals("securepassword123", second.getPassword());

        verify(authService).setRole("admin@xx.xx", "admin");
        verify(authService).setRole("documentvalidator@xx.xx", "mock_validator");

        verifyNoMoreInteractions(authService);
        verifyNoInteractions(dsl);
    }
}