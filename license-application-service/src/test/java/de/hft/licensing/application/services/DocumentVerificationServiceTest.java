package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.DocumentVerficationDslService;
import de.hft.licensing.db.enums.VerificationStatus;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.DocumentValidationCallbackRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentVerificationServiceTest {

    @Mock
    private DocumentVerficationDslService repository;

    private DocumentVerificationService service;

    @BeforeEach
    void setUp() {
        service = new DocumentVerificationService(repository);
    }

    @Test
    void handleValidationCallback_verified_updatesVerified_andReturns_whenUpdatedNonZero() {
        DocumentValidationCallbackRequest req = new DocumentValidationCallbackRequest();
        req.setApplicationId(1);
        req.setStatus(DocumentValidationCallbackRequest.StatusEnum.VERIFIED);

        when(repository.updateVerificationStatus(1, VerificationStatus.verified)).thenReturn(1);

        service.handleValidationCallback(req);

        verify(repository).updateVerificationStatus(1, VerificationStatus.verified);
        verify(repository, never()).updateRemarks(anyInt(), anyString());
    }

    @Test
    void handleValidationCallback_verified_updatesVerified_andReturns_whenUpdatedZero() {
        DocumentValidationCallbackRequest req = new DocumentValidationCallbackRequest();
        req.setApplicationId(1);
        req.setStatus(DocumentValidationCallbackRequest.StatusEnum.VERIFIED);

        when(repository.updateVerificationStatus(1, VerificationStatus.verified)).thenReturn(0);

        service.handleValidationCallback(req);

        verify(repository).updateVerificationStatus(1, VerificationStatus.verified);
        verify(repository, never()).updateRemarks(anyInt(), anyString());
    }

    @Test
    void handleValidationCallback_rejected_updatesRejected_andReturns_whenUpdatedZero() {
        DocumentValidationCallbackRequest req = new DocumentValidationCallbackRequest();
        req.setApplicationId(2);
        req.setStatus(DocumentValidationCallbackRequest.StatusEnum.REJECTED);
        req.setRejectionReason("bad doc");

        when(repository.updateVerificationStatus(2, VerificationStatus.rejected)).thenReturn(0);

        service.handleValidationCallback(req);

        verify(repository).updateVerificationStatus(2, VerificationStatus.rejected);
        verify(repository, never()).updateRemarks(anyInt(), anyString());
    }

    @Test
    void handleValidationCallback_rejected_updatesRejected_andUpdatesRemarks_whenReasonPresent_andUpdatedNonZero() {
        DocumentValidationCallbackRequest req = new DocumentValidationCallbackRequest();
        req.setApplicationId(3);
        req.setStatus(DocumentValidationCallbackRequest.StatusEnum.REJECTED);
        req.setRejectionReason("too blurry");

        when(repository.updateVerificationStatus(3, VerificationStatus.rejected)).thenReturn(1);

        service.handleValidationCallback(req);

        verify(repository).updateVerificationStatus(3, VerificationStatus.rejected);
        verify(repository).updateRemarks(3, "too blurry");
    }

    @Test
    void handleValidationCallback_rejected_updatesRejected_andDoesNotUpdateRemarks_whenReasonNull_andUpdatedNonZero() {
        DocumentValidationCallbackRequest req = new DocumentValidationCallbackRequest();
        req.setApplicationId(4);
        req.setStatus(DocumentValidationCallbackRequest.StatusEnum.REJECTED);
        req.setRejectionReason(null);

        when(repository.updateVerificationStatus(4, VerificationStatus.rejected)).thenReturn(1);

        service.handleValidationCallback(req);

        verify(repository).updateVerificationStatus(4, VerificationStatus.rejected);
        verify(repository, never()).updateRemarks(anyInt(), anyString());
    }


    @Test
    void getApplicationDocuments_returnsNull_whenApplicationNotFound() {
        when(repository.getApplication(10)).thenReturn(null);

        DocumentValidationCallbackRequest res = service.getApplicationDocuments(10);

        assertNull(res);
        verify(repository).getApplication(10);
    }

    @Test
    void getApplicationDocuments_returnsResponse_withId_andNoStatus_whenVerificationStatusNull() {
        ApplicationRecord rec = new ApplicationRecord();
        rec.setId(11);
        rec.setVerificationStatus(null);

        when(repository.getApplication(11)).thenReturn(rec);

        DocumentValidationCallbackRequest res = service.getApplicationDocuments(11);

        assertNotNull(res);
        assertEquals(11, res.getApplicationId());
        assertNull(res.getStatus());
    }

    @Test
    void getApplicationDocuments_mapsVerificationStatus_toEnum_whenPresent() {
        ApplicationRecord rec = new ApplicationRecord();
        rec.setId(12);
        rec.setVerificationStatus(VerificationStatus.rejected);

        when(repository.getApplication(12)).thenReturn(rec);

        DocumentValidationCallbackRequest res = service.getApplicationDocuments(12);

        assertNotNull(res);
        assertEquals(12, res.getApplicationId());
        assertEquals(DocumentValidationCallbackRequest.StatusEnum.REJECTED, res.getStatus());
    }
}