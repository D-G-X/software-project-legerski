package de.hft.validatormock.service;

import de.hft.validatormock.dto.ValidationRequest;
import de.hft.validatormock.dto.ValidationResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MockValidatorService {

    public ValidationResponse verifyDocuments(ValidationRequest validationRequest) {
        boolean isAddressValid = validationRequest.getPropertyAddress() != null &&
                (validationRequest.getPropertyAddress().toLowerCase().contains("mallorca")
                        || validationRequest.getPropertyAddress().toLowerCase().contains("palma"));

        String status;
        String message;

        if (isAddressValid) {
            status = "Valid";
            message = "Checked by mock validator.";
        } else {
            status = "Unvalid";
            message = "Address seems incorrect.";
        }

        return new ValidationResponse(status, message, LocalDateTime.now());
    }
}
