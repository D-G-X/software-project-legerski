package de.hft.validatormock.controller;

import de.hft.validatormock.dto.ValidationRequest;
import de.hft.validatormock.dto.ValidationResponse;
import de.hft.validatormock.service.MockValidatorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
public class MockValidatorController {

    @Autowired
    MockValidatorService mockValidatorService;

    @PostMapping("/documents/verification")
    @Valid
    public ValidationResponse validateDocuments(@RequestBody ValidationRequest request) {
        return mockValidatorService.verifyDocuments(request);
    }

}
