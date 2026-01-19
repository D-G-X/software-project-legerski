package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.tables.License;
import org.jooq.Condition;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseExpiryRoutineDslServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DefaultDSLContext dsl;

    private LicenseExpiryRoutineDslService service;

    @BeforeEach
    void setUp() {
        service = new LicenseExpiryRoutineDslService(dsl);
    }

    @Test
    void expireLicenses_executes() {
        when(dsl.update(License.LICENSE)
                .set(License.LICENSE.LICENSE_STATUS, LicenseStatus.expired)
                .where((Condition) any())
                .and((Condition) any())
                .execute()).thenReturn(3);

        assertEquals(3, service.expireLicenses());
    }
}