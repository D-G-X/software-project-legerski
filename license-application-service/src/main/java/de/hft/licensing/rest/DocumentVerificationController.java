package de.hft.licensing.rest;

import de.hft.licensing.api.DocumentVerificationApi;
import de.hft.licensing.db.enums.VerificationStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.DocumentValidationCallbackRequest;
import de.hft.licensing.services.auth.ApplicationAuthorizationService;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentVerificationController implements DocumentVerificationApi {

    private final DSLContext dsl;
    private final ApplicationAuthorizationService applicationAuthorization;

    public DocumentVerificationController(DSLContext dsl, ApplicationAuthorizationService applicationAuthorization) {
        this.dsl = dsl;
        this.applicationAuthorization = applicationAuthorization;
    }

    @Override
    @PreAuthorize("hasRole('mock_validator')")
    public ResponseEntity<Void> documentValidationCallback(DocumentValidationCallbackRequest documentValidationCallbackRequest) {
        if (documentValidationCallbackRequest.getStatus() == DocumentValidationCallbackRequest.StatusEnum.VERIFIED) {
            var updated = dsl.update(Application.APPLICATION)
                    .set(Application.APPLICATION.VERIFICATION_STATUS, VerificationStatus.verified)
                    .where(Application.APPLICATION.ID.eq(documentValidationCallbackRequest.getApplicationId()))
                    .execute();
            if (updated == 0) {
                System.out.println("Received validation callback for unknown application_id={" + documentValidationCallbackRequest.getApplicationId() + "}");
            }
        } else if (documentValidationCallbackRequest.getStatus() == DocumentValidationCallbackRequest.StatusEnum.REJECTED) {
            var updated = dsl.update(Application.APPLICATION)
                    .set(Application.APPLICATION.VERIFICATION_STATUS, VerificationStatus.rejected)
                    .where(Application.APPLICATION.ID.eq(documentValidationCallbackRequest.getApplicationId()))
                    .execute();
            if (updated == 0) {
                System.out.println("Received validation callback for unknown application_id={" + documentValidationCallbackRequest.getApplicationId() + "}");
            } else if (documentValidationCallbackRequest.getRejectionReason() != null) {
                dsl.update(Application.APPLICATION)
                        .set(Application.APPLICATION.REMARKS, documentValidationCallbackRequest.getRejectionReason())
                        .where(Application.APPLICATION.ID.eq(documentValidationCallbackRequest.getApplicationId()))
                        .execute();
            }
        }
        return null;
    }

    @Override
    @PreAuthorize("@applicationAuthorization.canAccessApplication(authentication, #applicationId)")
    public ResponseEntity<DocumentValidationCallbackRequest> getApplicationDocuments(Integer applicationId) {
        ApplicationRecord record = dsl.selectFrom(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOneInto(ApplicationRecord.class);

        if (record != null) {
            DocumentValidationCallbackRequest response = new DocumentValidationCallbackRequest();
            response.setApplicationId(record.getId());
            response.setStatus(DocumentValidationCallbackRequest.StatusEnum.fromValue(record.getVerificationStatus().getLiteral().toUpperCase()));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
