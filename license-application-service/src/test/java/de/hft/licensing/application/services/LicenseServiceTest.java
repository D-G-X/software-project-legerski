package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.LicenseDslService;
import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.tables.records.LicenseRecord;
import de.hft.licensing.model.UpdateLicenseStatusRequest;
import de.hft.licensing.model.LicenseStatusApiEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicenseServiceTest {

    @Mock
    private LicenseDslService repository;

    private LicenseService service;

    @BeforeEach
    void setUp() {
        service = new LicenseService(repository);
    }

    @Test
    void deleteLicense_returnsTrue_whenDeletedRowsGreaterThanZero() {
        when(repository.deleteLicense(1)).thenReturn(1);

        assertTrue(service.deleteLicense(1));
        verify(repository).deleteLicense(1);
    }

    @Test
    void deleteLicense_returnsFalse_whenDeletedRowsZero() {
        when(repository.deleteLicense(1)).thenReturn(0);

        assertFalse(service.deleteLicense(1));
        verify(repository).deleteLicense(1);
    }

    @Test
    void getLicenseWithCadastral_delegatesToRepository() {
        LicenseDslService.LicenseWithCadastralReference rec = mock(LicenseDslService.LicenseWithCadastralReference.class);
        when(repository.getLicenseWithCadastral(10)).thenReturn(rec);

        assertSame(rec, service.getLicenseWithCadastral(10));
    }

    @Test
    void listLicensesWithCadastral_callsByUserId_whenUserIdNotNull() {
        UUID userId = UUID.randomUUID();
        List<LicenseDslService.LicenseWithCadastralReference> list = List.of(mock(LicenseDslService.LicenseWithCadastralReference.class));
        when(repository.listLicensesWithCadastralByUserId(userId.toString())).thenReturn(list);

        assertSame(list, service.listLicensesWithCadastral(userId));
        verify(repository).listLicensesWithCadastralByUserId(userId.toString());
        verify(repository, never()).listAllLicensesWithCadastral();
    }

    @Test
    void listLicensesWithCadastral_callsAll_whenUserIdNull() {
        List<LicenseDslService.LicenseWithCadastralReference> list = List.of(mock(LicenseDslService.LicenseWithCadastralReference.class));
        when(repository.listAllLicensesWithCadastral()).thenReturn(list);

        assertSame(list, service.listLicensesWithCadastral(null));
        verify(repository).listAllLicensesWithCadastral();
        verify(repository, never()).listLicensesWithCadastralByUserId(anyString());
    }

    @Test
    void updateLicenseStatus_returnsNull_whenRepositoryReturnsNull() {
        int licenseId = 5;

        when(repository.getLicenseStatus(licenseId)).thenReturn(LicenseStatus.active);
        when(repository.updateLicenseStatus(eq(licenseId), any(LicenseStatus.class))).thenReturn(null);

        UpdateLicenseStatusRequest req = new UpdateLicenseStatusRequest();
        req.setLicenseStatus(anyApiStatus());

        assertNull(service.updateLicenseStatus(licenseId, req));
    }

    @Test
    void updateLicenseStatus_returnsRecord_andCoversOldStatusNullBranch() {
        int licenseId = 6;

        when(repository.getLicenseStatus(licenseId)).thenReturn(null);

        LicenseRecord updated = new LicenseRecord();
        updated.setId(licenseId);
        updated.setLicenseStatus(LicenseStatus.active);

        when(repository.updateLicenseStatus(eq(licenseId), any(LicenseStatus.class))).thenReturn(updated);

        UpdateLicenseStatusRequest req = new UpdateLicenseStatusRequest();
        req.setLicenseStatus(anyApiStatus());

        LicenseRecord res = service.updateLicenseStatus(licenseId, req);

        assertNotNull(res);
        assertEquals(licenseId, res.getId());
    }

    @Test
    void updateLicenseStatus_returnsRecord_andCoversNoChangeBranch() {
        int licenseId = 7;

        when(repository.getLicenseStatus(licenseId)).thenReturn(LicenseStatus.active);

        LicenseRecord updated = new LicenseRecord();
        updated.setId(licenseId);
        updated.setLicenseStatus(LicenseStatus.active);

        when(repository.updateLicenseStatus(eq(licenseId), any(LicenseStatus.class))).thenReturn(updated);

        UpdateLicenseStatusRequest req = new UpdateLicenseStatusRequest();
        req.setLicenseStatus(anyApiStatus());

        LicenseRecord res = service.updateLicenseStatus(licenseId, req);

        assertNotNull(res);
        assertEquals(LicenseStatus.active, res.getLicenseStatus());
    }

    @Test
    void updateLicenseStatus_returnsRecord_andCoversChangedBranch() {
        int licenseId = 8;

        when(repository.getLicenseStatus(licenseId)).thenReturn(LicenseStatus.suspended);

        LicenseRecord updated = new LicenseRecord();
        updated.setId(licenseId);
        updated.setLicenseStatus(LicenseStatus.active);

        when(repository.updateLicenseStatus(eq(licenseId), any(LicenseStatus.class))).thenReturn(updated);

        UpdateLicenseStatusRequest req = new UpdateLicenseStatusRequest();
        req.setLicenseStatus(anyApiStatus());

        LicenseRecord res = service.updateLicenseStatus(licenseId, req);

        assertNotNull(res);
        assertEquals(LicenseStatus.active, res.getLicenseStatus());
    }

    private static LicenseStatusApiEnum anyApiStatus() {
        return LicenseStatusApiEnum.values()[0];
    }
}