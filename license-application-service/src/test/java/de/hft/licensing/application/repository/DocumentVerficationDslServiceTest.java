package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.VerificationStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
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
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentVerficationDslServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DSLContext dsl;

    private DocumentVerficationDslService service;

    @BeforeEach
    void setUp() {
        service = new DocumentVerficationDslService(dsl);
    }

    @Test
    void updateVerificationStatus_executes() {
        when(dsl.update(Application.APPLICATION)
                .set(eq(Application.APPLICATION.VERIFICATION_STATUS), eq(VerificationStatus.verified))
                .where((Condition) any())
                .execute()).thenReturn(1);

        assertEquals(1, service.updateVerificationStatus(1, VerificationStatus.verified));
    }

    @Test
    void updateRemarks_executes() {
        when(dsl.update(Application.APPLICATION)
                .set(eq(Application.APPLICATION.REMARKS), eq("r"))
                .where((Condition) any())
                .execute()).thenReturn(1);

        assertEquals(1, service.updateRemarks(1, "r"));
    }

    @Test
    void getApplication_fetchesRecord() {
        ApplicationRecord rec = new ApplicationRecord();
        when(dsl.selectFrom(Application.APPLICATION)
                .where((Condition) any())
                .fetchOneInto(ApplicationRecord.class)).thenReturn(rec);

        assertSame(rec, service.getApplication(1));
    }
}