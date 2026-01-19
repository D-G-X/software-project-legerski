package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.tables.License;
import de.hft.licensing.db.tables.records.LicenseRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseDslServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DSLContext dsl;

    private LicenseDslService service;

    @BeforeEach
    void setUp() {
        service = new LicenseDslService(dsl);
    }

    @Test
    void deleteLicense_executes() {
        when(dsl.deleteFrom(License.LICENSE).where((Condition) any()).execute()).thenReturn(1);
        assertEquals(1, service.deleteLicense(1));
    }


    @Test
    void getLicenseStatus_fetches() {
        when(dsl.select(License.LICENSE.LICENSE_STATUS)
                .from(License.LICENSE)
                .where((Condition) any())
                .fetchOneInto(any(Class.class))).thenReturn(LicenseStatus.active);

        assertEquals(LicenseStatus.active, service.getLicenseStatus(1));
    }

    @Test
    void updateLicenseStatus_returnsRecord() {
        LicenseRecord lr = new LicenseRecord();
        when(dsl.update(License.LICENSE)
                .set(eq(License.LICENSE.LICENSE_STATUS), eq(LicenseStatus.active))
                .where((Condition) any())
                .returning()
                .fetchOneInto(LicenseRecord.class)).thenReturn(lr);

        assertSame(lr, service.updateLicenseStatus(1, LicenseStatus.active));
    }
}