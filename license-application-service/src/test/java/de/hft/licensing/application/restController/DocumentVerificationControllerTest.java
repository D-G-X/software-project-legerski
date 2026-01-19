package de.hft.licensing.application.restController;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.application.services.DocumentVerificationService;
import de.hft.licensing.application.services.authServices.ApplicationAuthorizationService;
import de.hft.licensing.model.DocumentValidationCallbackRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentVerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://dummy-issuer"
})
class DocumentVerificationControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean DocumentVerificationService documentVerificationService;

    @MockitoBean(name = "applicationAuthorization")
    ApplicationAuthorizationService applicationAuthorization;

    @BeforeEach
    void allowAccess() {
        lenient().when(applicationAuthorization.canAccessApplication(any(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser(roles = "mock_validator")
    void documentValidationCallback_returns400_whenBodyNull() throws Exception {
        mvc.perform(post("/validation-callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(documentVerificationService);
    }

    @Test
    @WithMockUser(roles = "mock_validator")
    void documentValidationCallback_returns400_whenMissingFields() throws Exception {
        mvc.perform(post("/validation-callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(documentVerificationService);
    }

    @Test
    @WithMockUser(roles = "mock_validator")
    void documentValidationCallback_returns200_andDelegates() throws Exception {
        var req = new DocumentValidationCallbackRequest();
        req.setApplicationId(1);
        req.setStatus(DocumentValidationCallbackRequest.StatusEnum.VERIFIED);

        mvc.perform(post("/validation-callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(documentVerificationService).handleValidationCallback(any(DocumentValidationCallbackRequest.class));
    }

    @Test
    @WithMockUser(roles = "user")
    void getApplicationDocuments_returns400_whenIdNull_notPossibleOverHttp_butEmptyPathGives404() throws Exception {
        mvc.perform(get("/applications//documents"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "user")
    void getApplicationDocuments_returns404_whenServiceReturnsNull() throws Exception {
        when(documentVerificationService.getApplicationDocuments(5)).thenReturn(null);

        mvc.perform(get("/applications/5/documents"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "user")
    void getApplicationDocuments_returns200_whenPresent() throws Exception {
        var resp = new DocumentValidationCallbackRequest();
        resp.setApplicationId(5);
        resp.setStatus(DocumentValidationCallbackRequest.StatusEnum.VERIFIED);

        when(documentVerificationService.getApplicationDocuments(5)).thenReturn(resp);

        mvc.perform(get("/applications/5/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application_id").value(5))
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }
}