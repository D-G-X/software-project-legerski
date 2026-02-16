package de.hft.licensing.application.services;


import de.hft.licensing.application.repository.ApplicationDslService;
import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.*;
import org.jooq.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationDslService dslService;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    private ApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ApplicationService(emailService, userService, dslService);
    }

    @Test
    void createApplication_returnsBALLOT_PERIOD_INACTIVE_whenNoActivePeriod() {
        UUID userId = UUID.randomUUID();

        when(dslService.isBallotPeriodActive(any(LocalDateTime.class))).thenReturn(false);

        var res = service.createApplication(userId, anyLicenseTypeApi(), "cad-1", "r");

        assertEquals(ApplicationService.CreateApplicationResultCode.BALLOT_PERIOD_INACTIVE, res.code());
        assertNull(res.record());

        verify(dslService, never()).userExists(any());
        verify(dslService, never()).createApplication(any(), any(), any(), any(), any());
        verify(dslService, never()).getCurrentBallotPeriodId(any());
        verify(dslService, never()).createBallotEntry(anyInt(), anyInt());
    }

    @Test
    void createApplication_returnsUSER_NOT_FOUND_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(dslService.isBallotPeriodActive(any(LocalDateTime.class))).thenReturn(true);
        when(dslService.userExists(userId)).thenReturn(false);

        var res = service.createApplication(userId, anyLicenseTypeApi(), "cad-1", "r");

        assertEquals(ApplicationService.CreateApplicationResultCode.USER_NOT_FOUND, res.code());
        assertNull(res.record());

        verify(dslService, never()).createApplication(any(), any(), any(), any(), any());
        verify(dslService, never()).getCurrentBallotPeriodId(any());
        verify(dslService, never()).createBallotEntry(anyInt(), anyInt());
    }

    @Test
    void createApplication_returnsINTERNAL_ERROR_whenCreateApplicationReturnsNull() {
        UUID userId = UUID.randomUUID();

        when(dslService.isBallotPeriodActive(any(LocalDateTime.class))).thenReturn(true);
        when(dslService.userExists(userId)).thenReturn(true);
        when(dslService.createApplication(eq(userId), anyString(), any(LicenseType.class), anyString(), any(LocalDateTime.class)))
                .thenReturn(null);

        var res = service.createApplication(userId, anyLicenseTypeApi(), "cad-1", "r");

        assertEquals(ApplicationService.CreateApplicationResultCode.INTERNAL_ERROR, res.code());
        assertNull(res.record());

        verify(dslService, never()).getCurrentBallotPeriodId(any());
        verify(dslService, never()).createBallotEntry(anyInt(), anyInt());
    }

    @Test
    void createApplication_returnsINTERNAL_ERROR_whenCurrentBallotPeriodIdIsNull() {
        UUID userId = UUID.randomUUID();

        when(dslService.isBallotPeriodActive(any(LocalDateTime.class))).thenReturn(true);
        when(dslService.userExists(userId)).thenReturn(true);

        ApplicationRecord created = new ApplicationRecord();
        created.setId(123);
        when(dslService.createApplication(eq(userId), anyString(), any(LicenseType.class), anyString(), any(LocalDateTime.class)))
                .thenReturn(created);

        when(dslService.getCurrentBallotPeriodId(any(LocalDateTime.class))).thenReturn(null);

        var res = service.createApplication(userId, anyLicenseTypeApi(), "cad-1", "r");

        assertEquals(ApplicationService.CreateApplicationResultCode.INTERNAL_ERROR, res.code());
        assertNull(res.record());

        verify(dslService, never()).createBallotEntry(anyInt(), anyInt());
    }

    @Test
    void createApplication_returnsINTERNAL_ERROR_whenCreateBallotEntryFails() {
        UUID userId = UUID.randomUUID();

        when(dslService.isBallotPeriodActive(any(LocalDateTime.class))).thenReturn(true);
        when(dslService.userExists(userId)).thenReturn(true);

        ApplicationRecord created = new ApplicationRecord();
        created.setId(123);
        when(dslService.createApplication(eq(userId), anyString(), any(LicenseType.class), anyString(), any(LocalDateTime.class)))
                .thenReturn(created);

        when(dslService.getCurrentBallotPeriodId(any(LocalDateTime.class))).thenReturn(77);
        when(dslService.createBallotEntry(77, 123)).thenReturn(0);

        var res = service.createApplication(userId, anyLicenseTypeApi(), "cad-1", "r");

        assertEquals(ApplicationService.CreateApplicationResultCode.INTERNAL_ERROR, res.code());
        assertNull(res.record());
    }

    @Test
    void createApplication_returnsOK_whenAllStepsSucceed() {
        UUID userId = UUID.randomUUID();

        when(dslService.isBallotPeriodActive(any(LocalDateTime.class))).thenReturn(true);
        when(dslService.userExists(userId)).thenReturn(true);

        ApplicationRecord created = new ApplicationRecord();
        created.setId(123);

        when(dslService.createApplication(eq(userId), eq("cad-1"), any(LicenseType.class), eq("remarks"), any(LocalDateTime.class)))
                .thenReturn(created);

        when(dslService.getCurrentBallotPeriodId(any(LocalDateTime.class))).thenReturn(77);
        when(dslService.createBallotEntry(77, 123)).thenReturn(1);

        var res = service.createApplication(userId, anyLicenseTypeApi(), "cad-1", "remarks");

        assertEquals(ApplicationService.CreateApplicationResultCode.OK, res.code());
        assertNotNull(res.record());
        assertEquals(123, res.record().getId());

        verify(dslService).createBallotEntry(77, 123);
    }


    @Test
    void deleteApplication_returnsTrue_whenRepositoryDeletesRow() {
        when(dslService.deleteApplication(10)).thenReturn(1);
        assertTrue(service.deleteApplication(10));
    }

    @Test
    void deleteApplication_returnsFalse_whenRepositoryDeletesNothing() {
        when(dslService.deleteApplication(10)).thenReturn(0);
        assertFalse(service.deleteApplication(10));
    }

    @Test
    void getApplication_delegatesToRepository() {
        ApplicationRecord rec = new ApplicationRecord();
        rec.setId(5);
        when(dslService.getApplication(5)).thenReturn(rec);

        assertSame(rec, service.getApplication(5));
    }

    @Test
    void listApplications_passesNullStatus_whenApiStatusNull() {
        UUID userId = UUID.randomUUID();
        when(dslService.listApplications(eq(userId), isNull())).thenReturn(List.of());

        service.listApplications(userId, null);

        verify(dslService).listApplications(eq(userId), isNull());
    }


    @Test
    void updateApplication_returnsNOT_FOUND_whenRepositoryReturnsNull() {
        int appId = 42;

        when(dslService.getApplicationStatus(appId)).thenReturn(ApplicationStatus.payment_received);
        when(dslService.getApplicationRemarks(appId)).thenReturn("old");

        // update call returns null => NOT_FOUND
        when(dslService.updateApplication(eq(appId), anyMap())).thenReturn(null);

        ApplicationUpdate update = new ApplicationUpdate();
        update.setRemarks("new");

        var res = service.updateApplication(appId, update);

        assertEquals(ApplicationService.UpdateApplicationResultCode.NOT_FOUND, res.code());
        assertNull(res.record());
    }

    @Test
    void updateApplication_buildsUpdateMap_correctly_andReturnsOK() {
        int appId = 42;
        UUID userId = UUID.randomUUID();

        when(dslService.getApplicationStatus(appId)).thenReturn(ApplicationStatus.payment_received);
        when(dslService.getApplicationRemarks(appId)).thenReturn("old-remarks");

        ApplicationRecord updated = new ApplicationRecord();
        updated.setUserId(userId.toString());
        updated.setId(appId);
        updated.setApplicationStatus(ApplicationStatus.approved);
        updated.setRemarks("new-remarks");

        NotificationPreferencesResource prefs = new NotificationPreferencesResource();
        prefs.setId(1);
        prefs.setUserId(userId);
        prefs.setNotificationWay(NotificationWayApiEnum.EMAIL);
        prefs.setApplicationUpdatesNotification(true);
        prefs.setLicenseRenewalNotification(true);


        ArgumentCaptor<Map<Field<?>, Object>> captor = ArgumentCaptor.forClass(Map.class);

        when(dslService.updateApplication(eq(appId), anyMap())).thenReturn(updated);
        when(userService.getNotificationPreferences(UUID.fromString(updated.getUserId()))).thenReturn(prefs);

        ApplicationUpdate update = new ApplicationUpdate();
        update.setApplicationStatus(ApplicationStatusApiEnum.APPROVED);
        update.setRemarks("new-remarks");
        update.setCadastralReference("cad-99");
        update.setLicenseType(LicenseTypeApiEnum.values()[0]);

        var res = service.updateApplication(appId, update);

        assertEquals(ApplicationService.UpdateApplicationResultCode.OK, res.code());
        assertNotNull(res.record());
        assertEquals(appId, res.record().getId());

        verify(dslService).updateApplication(eq(appId), captor.capture());
        Map<Field<?>, Object> updates = captor.getValue();

        assertTrue(updates.containsKey(Application.APPLICATION.REMARKS));
        assertEquals("new-remarks", updates.get(Application.APPLICATION.REMARKS));

        assertTrue(updates.containsKey(Application.APPLICATION.CADASTRAL_REFERENCE));
        assertEquals("cad-99", updates.get(Application.APPLICATION.CADASTRAL_REFERENCE));

        assertTrue(updates.containsKey(Application.APPLICATION.APPLICATION_STATUS));
        assertNotNull(updates.get(Application.APPLICATION.APPLICATION_STATUS));

        assertTrue(updates.containsKey(Application.APPLICATION.LICENSE_TYPE));
        assertNotNull(updates.get(Application.APPLICATION.LICENSE_TYPE));

        assertTrue(updates.containsKey(Application.APPLICATION.CHANGED_AT));
        assertNotNull(updates.get(Application.APPLICATION.CHANGED_AT));
    }


    private static LicenseTypeApiEnum anyLicenseTypeApi() {
        return LicenseTypeApiEnum.values()[0];
    }
}