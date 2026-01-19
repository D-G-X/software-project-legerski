package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.ApplicationDslService;
import de.hft.licensing.application.repository.DistributionAlgorithmDslService;
import de.hft.licensing.application.repository.PaymentDslService;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.ApplicationPaymentCreate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentDslService repository;

    @Mock
    private DistributionAlgorithmDslService distributionAlgorithmDslService;

    @Mock
    private ApplicationDslService applicationDslService;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(repository, distributionAlgorithmDslService, applicationDslService);
    }

    @Test
    void createPayment_callsRepositoryWithCalculatedFee_andUnpaidStatus_andCurrentPeriodId() {
        int applicationId = 10;

        ApplicationRecord app = new ApplicationRecord();
        app.setId(applicationId);
        app.setLicenseType(LicenseType.etv);

        when(applicationDslService.getApplication(applicationId)).thenReturn(app);
        when(distributionAlgorithmDslService.getCurrentBallotPeriodId()).thenReturn(77);

        ApplicationPaymentCreate create = new ApplicationPaymentCreate();
        create.setName("Max");
        create.setIban("DE123");
        create.setBic("BIC123");

        ApplicationPaymentRecord created = new ApplicationPaymentRecord();
        when(repository.createPayment(
                eq(applicationId),
                any(LocalDateTime.class),
                eq(new BigDecimal(3500)),
                eq("Max"),
                eq("DE123"),
                eq("BIC123"),
                eq(PaymentStatus.unpaid),
                eq(77)
        )).thenReturn(created);

        ApplicationPaymentRecord res = service.createPayment(applicationId, create);

        assertSame(created, res);

        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).createPayment(
                eq(applicationId),
                timeCaptor.capture(),
                eq(new BigDecimal(3500)),
                eq("Max"),
                eq("DE123"),
                eq("BIC123"),
                eq(PaymentStatus.unpaid),
                eq(77)
        );
        assertNotNull(timeCaptor.getValue());
    }

    @Test
    void listPayments_returnsNull_whenApplicationDoesNotExist() {
        when(repository.applicationExists(1)).thenReturn(false);

        assertNull(service.listPayments(1));
        verify(repository, never()).listPayments(anyInt());
    }

    @Test
    void listPayments_returnsList_whenApplicationExists() {
        when(repository.applicationExists(1)).thenReturn(true);

        List<ApplicationPaymentRecord> list = List.of(new ApplicationPaymentRecord());
        when(repository.listPayments(1)).thenReturn(list);

        assertSame(list, service.listPayments(1));
        verify(repository).listPayments(1);
    }

    @Test
    void getApplication_delegatesToRepository() {
        ApplicationRecord rec = new ApplicationRecord();
        when(repository.getApplication(5)).thenReturn(rec);

        assertSame(rec, service.getApplication(5));
    }

    @Test
    void calculateFeeAmount_returnsETVAmount() {
        ApplicationRecord app = new ApplicationRecord();
        app.setLicenseType(LicenseType.etv);

        when(applicationDslService.getApplication(1)).thenReturn(app);

        assertEquals(new BigDecimal(3500), service.calculateFeeAmount(1));
    }

    @Test
    void calculateFeeAmount_returnsETVPLAmount() {
        ApplicationRecord app = new ApplicationRecord();
        app.setLicenseType(LicenseType.etvpl);

        when(applicationDslService.getApplication(1)).thenReturn(app);

        assertEquals(new BigDecimal(875), service.calculateFeeAmount(1));
    }

    @Test
    void calculateFeeAmount_returnsETV60Amount() {
        ApplicationRecord app = new ApplicationRecord();
        app.setLicenseType(LicenseType.etv60);

        when(applicationDslService.getApplication(1)).thenReturn(app);

        assertEquals(new BigDecimal(290), service.calculateFeeAmount(1));
    }

    @Test
    void calculateFeeAmount_returnsZero_forUnknownType() {
        ApplicationRecord app = new ApplicationRecord();
        app.setLicenseType(LicenseType.values()[0]);

        when(applicationDslService.getApplication(1)).thenReturn(app);

        BigDecimal res = service.calculateFeeAmount(1);

        if (app.getLicenseType() == LicenseType.etv) {
            assertEquals(new BigDecimal(3500), res);
        } else if (app.getLicenseType() == LicenseType.etvpl) {
            assertEquals(new BigDecimal(875), res);
        } else if (app.getLicenseType() == LicenseType.etv60) {
            assertEquals(new BigDecimal(290), res);
        } else {
            assertEquals(BigDecimal.ZERO, res);
        }
    }
}