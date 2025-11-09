package de.hft.licensing.rest;

import de.hft.licensing.api.ApplicationsApi;
import de.hft.licensing.model.ApplicationCreate;
import de.hft.licensing.model.ApplicationDocumentRecord;
import de.hft.licensing.model.ApplicationPaymentCreate;
import de.hft.licensing.model.ApplicationPaymentRecord;
import de.hft.licensing.model.ApplicationRecord;
import de.hft.licensing.model.ApplicationUpdate;
import de.hft.licensing.model.RunLottery200Response;
import de.hft.licensing.model.RunLotteryRequest;
import de.hft.licensing.model.UpdatePaymentRequest;
import de.hft.licensing.model.VerifyDocumentRequest;
import org.jooq.DSLContext;
import de.hft.licensing.db.tables.Application;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class ApplicationController implements ApplicationsApi {

    private final DSLContext dsl;

    public ApplicationController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ResponseEntity<ApplicationRecord> createApplication(ApplicationCreate applicationCreate) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationPaymentRecord> createPayment(Integer applicationId, ApplicationPaymentCreate applicationPaymentCreate) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteApplication(Integer applicationId) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationRecord> getApplication(Integer applicationId) {
        var result = dsl.select()
            .from(Application.APPLICATION)
            .where(Application.APPLICATION.ID.eq(applicationId)).fetchOneInto(ApplicationRecord.class);

        return result != null
                ? ResponseEntity.ok(result)
                : ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<List<ApplicationRecord>> listApplications(UUID userId, de.hft.licensing.model.ApplicationStatus applicationStatus) {
        return null;
    }

    @Override
    public ResponseEntity<List<ApplicationDocumentRecord>> listDocuments(Integer applicationId) {
        return null;
    }

    @Override
    public ResponseEntity<List<ApplicationPaymentRecord>> listPayments(Integer applicationId) {
        return null;
    }

    @Override
    public ResponseEntity<RunLottery200Response> runLottery(RunLotteryRequest runLotteryRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationRecord> updateApplication(Integer applicationId, ApplicationUpdate applicationUpdate) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationPaymentRecord> updatePayment(Integer applicationId, Integer paymentId, UpdatePaymentRequest updatePaymentRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationDocumentRecord> uploadDocument(Integer applicationId, MultipartFile file, String documentType) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationDocumentRecord> verifyDocument(Integer applicationId, Integer documentId, VerifyDocumentRequest verifyDocumentRequest) {
        return null;
    }
}
