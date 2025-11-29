package de.hft.licensing.rest;

import de.hft.licensing.api.DocumentVerificationApi;
import de.hft.licensing.model.ApplicationDocumentResource;
import de.hft.licensing.model.VerifyDocumentRequest;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class DocumentVerificationController implements DocumentVerificationApi {

    private final DSLContext dsl;

    public DocumentVerificationController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ResponseEntity<List<ApplicationDocumentResource>> listDocuments(Integer applicationId) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationDocumentResource> uploadDocument(Integer applicationId, MultipartFile file, String documentType) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationDocumentResource> verifyDocument(Integer applicationId, Integer documentId, VerifyDocumentRequest verifyDocumentRequest) {
        return null;
    }
}
