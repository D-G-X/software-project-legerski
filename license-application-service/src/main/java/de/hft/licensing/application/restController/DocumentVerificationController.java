package de.hft.licensing.application.restController;

import de.hft.licensing.api.DocumentVerificationApi;
import de.hft.licensing.model.DocumentValidationCallbackRequest;
import de.hft.licensing.application.services.DocumentVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentVerificationController implements DocumentVerificationApi {

    private final DocumentVerificationService documentVerificationService;

    public DocumentVerificationController(DocumentVerificationService documentVerificationService) {
        this.documentVerificationService = documentVerificationService;
    }

    @Override
    @PreAuthorize("hasRole('mock_validator')")
    public ResponseEntity<Void> documentValidationCallback(DocumentValidationCallbackRequest req) {
        if (req == null || req.getApplicationId() == null || req.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        documentVerificationService.handleValidationCallback(req);

        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("@applicationAuthorization.canAccessApplication(authentication, #applicationId)")
    public ResponseEntity<DocumentValidationCallbackRequest> getApplicationDocuments(Integer applicationId) {
        if (applicationId == null) {
            return ResponseEntity.badRequest().build();
        }

        DocumentValidationCallbackRequest response = documentVerificationService.getApplicationDocuments(applicationId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}