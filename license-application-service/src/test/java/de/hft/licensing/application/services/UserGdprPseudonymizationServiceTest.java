package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.UserDslService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGdprPseudonymizationServiceTest {

    @Mock
    private UserDslService dsl;

    private UserGdprPseudonymizationService service;

    @BeforeEach
    void setUp() {
        service = new UserGdprPseudonymizationService(dsl);
        ReflectionTestUtils.setField(service, "pepper", "pepper".getBytes());
    }

    @Test
    void pseudonymizeUserIdEverywhere_returnsNull_whenOldUserIdNull() {
        assertNull(service.pseudonymizeUserIdEverywhere(null));
        verifyNoInteractions(dsl);
    }

    @Test
    void pseudonymizeUserIdEverywhere_returnsNull_whenUserDoesNotExist() {
        UUID oldUserId = UUID.randomUUID();
        when(dsl.userExists(oldUserId.toString())).thenReturn(false);

        assertNull(service.pseudonymizeUserIdEverywhere(oldUserId));
        verify(dsl).userExists(oldUserId.toString());
        verifyNoMoreInteractions(dsl);
    }

}