package de.hft.licensing.application.services;

import de.hft.licensing.db.enums.VerificationStatus;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.DocumentValidationCallbackRequest;
import de.hft.licensing.application.repository.DocumentVerficationDslService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentVerificationService {

    private final DocumentVerficationDslService repository;
    private final Logger log = LicensingLoggerFactory.getLogger(DocumentVerificationService.class);

    public DocumentVerificationService(DocumentVerficationDslService repository) {
        this.repository = repository;
    }

    @Transactional
    public void handleValidationCallback(DocumentValidationCallbackRequest req) {
        if (req.getStatus() == DocumentValidationCallbackRequest.StatusEnum.VERIFIED) {
            int updated = repository.updateVerificationStatus(req.getApplicationId(), VerificationStatus.verified);
            if (updated == 0) {
                log.warn("Received validation callback for unknown application_id={{}}", req.getApplicationId());
            }
            return;
        }

        if (req.getStatus() == DocumentValidationCallbackRequest.StatusEnum.REJECTED) {
            int updated = repository.updateVerificationStatus(req.getApplicationId(), VerificationStatus.rejected);
            if (updated == 0) {
                log.warn("Received validation callback for unknown application_id={{}}}", req.getApplicationId());
                return;
            }
            if (req.getRejectionReason() != null) {
                repository.updateRemarks(req.getApplicationId(), req.getRejectionReason());
            }
        }
    }

    @Transactional(readOnly = true)
    public DocumentValidationCallbackRequest getApplicationDocuments(Integer applicationId) {
        ApplicationRecord record = repository.getApplication(applicationId);
        if (record == null) {
            return null;
        }

        DocumentValidationCallbackRequest response = new DocumentValidationCallbackRequest();
        response.setApplicationId(record.getId());

        if (record.getVerificationStatus() != null) {
            response.setStatus(
                    DocumentValidationCallbackRequest.StatusEnum.fromValue(
                            record.getVerificationStatus().getLiteral().toUpperCase()
                    )
            );
        }

        return response;
    }
}