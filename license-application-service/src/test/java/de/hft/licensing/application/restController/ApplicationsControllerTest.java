package de.hft.licensing.application.restController;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.application.services.ApplicationService;
import de.hft.licensing.application.services.authServices.ApplicationAuthorizationService;
import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.ApplicationCreate;
import de.hft.licensing.model.ApplicationStatusApiEnum;
import de.hft.licensing.model.ApplicationUpdate;
import de.hft.licensing.model.LicenseTypeApiEnum;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationsController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://dummy-issuer"
})
class ApplicationsControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ApplicationService applicationService;

    @MockitoBean(name = "applicationAuthorization")
    ApplicationAuthorizationService applicationAuthorization;

    @BeforeEach
    void allowAllAuthorizations() {
        lenient().when(applicationAuthorization.canCreateApplication(any(), any())).thenReturn(true);
        lenient().when(applicationAuthorization.canAccessApplication(any(), any())).thenReturn(true);
        lenient().when(applicationAuthorization.canListApplications(any(), any())).thenReturn(true);
    }

    // ---------------------------
    // CREATE
    // ---------------------------

    @Test
    @WithMockUser
    void createApplication_returns400_whenBodyMissingRequiredFields() throws Exception {
        var req = new ApplicationCreate();

        mvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createApplication_returns400_whenCadastralInvalid() throws Exception {
        var req = new ApplicationCreate();
        req.setUserId(UUID.randomUUID());
        req.setLicenseType(LicenseTypeApiEnum.ETV);
        req.setCadastralReference("invalid");

        mvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createApplication_returns409_whenBallotPeriodInactive() throws Exception {
        var req = validCreate();

        var result = mock(ApplicationService.CreateApplicationResult.class);
        when(result.code()).thenReturn(ApplicationService.CreateApplicationResultCode.BALLOT_PERIOD_INACTIVE);
        when(result.record()).thenReturn(null);

        when(applicationService.createApplication(any(), any(), any(), any())).thenReturn(result);

        mvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void createApplication_returns422_whenUserNotFound() throws Exception {
        var req = validCreate();

        var result = mock(ApplicationService.CreateApplicationResult.class);
        when(result.code()).thenReturn(ApplicationService.CreateApplicationResultCode.USER_NOT_FOUND);
        when(result.record()).thenReturn(null);

        when(applicationService.createApplication(any(), any(), any(), any())).thenReturn(result);

        mvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser
    void createApplication_returns500_whenInternalError() throws Exception {
        var req = validCreate();

        var result = mock(ApplicationService.CreateApplicationResult.class);
        when(result.code()).thenReturn(ApplicationService.CreateApplicationResultCode.INTERNAL_ERROR);
        when(result.record()).thenReturn(null);

        when(applicationService.createApplication(any(), any(), any(), any())).thenReturn(result);

        mvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void createApplication_returns201_whenOk() throws Exception {
        var req = validCreate();
        var createdId = 201;
        var rec = fullApplicationRecord(createdId, req.getUserId());

        var result = mock(ApplicationService.CreateApplicationResult.class);
        when(result.code()).thenReturn(ApplicationService.CreateApplicationResultCode.OK);
        when(result.record()).thenReturn(rec);

        when(applicationService.createApplication(
                eq(req.getUserId()),
                eq(req.getLicenseType()),
                eq(req.getCadastralReference()),
                eq(req.getRemarks())
        )).thenReturn(result);

        mvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/applications/" + createdId))
                .andExpect(jsonPath("$.id").value(createdId));
    }

    // ---------------------------
    // GET
    // ---------------------------

    @Test
    @WithMockUser
    void getApplication_returns404_whenNotFound() throws Exception {
        when(applicationService.getApplication(7)).thenReturn(null);

        mvc.perform(get("/applications/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getApplication_returns200_whenPresent() throws Exception {
        var userId = UUID.randomUUID();
        when(applicationService.getApplication(7)).thenReturn(fullApplicationRecord(7, userId));

        mvc.perform(get("/applications/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    // ---------------------------
    // LIST
    // ---------------------------

    @Test
    @WithMockUser
    void listApplications_returns200_withArray() throws Exception {
        var userId = UUID.randomUUID();

        when(applicationService.listApplications(eq(userId), eq(ApplicationStatusApiEnum.SUBMITTED)))
                .thenReturn(List.of(fullApplicationRecord(1, userId)));

        mvc.perform(get("/applications")
                        .queryParam("user_id", userId.toString())
                        .queryParam("application_status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // ---------------------------
    // UPDATE
    // ---------------------------

    @Test
    @WithMockUser
    void updateApplication_returns400_whenBodyEmpty() throws Exception {
        var req = new ApplicationUpdate();

        mvc.perform(patch("/applications/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateApplication_returns404_whenNotFound() throws Exception {
        var req = new ApplicationUpdate();
        req.setRemarks("x");

        var result = mock(ApplicationService.UpdateApplicationResult.class);
        when(result.code()).thenReturn(ApplicationService.UpdateApplicationResultCode.NOT_FOUND);
        when(result.record()).thenReturn(null);

        when(applicationService.updateApplication(eq(5), any(ApplicationUpdate.class))).thenReturn(result);

        mvc.perform(patch("/applications/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateApplication_returns500_whenInternalError() throws Exception {
        var req = new ApplicationUpdate();
        req.setRemarks("x");

        var result = mock(ApplicationService.UpdateApplicationResult.class);
        when(result.code()).thenReturn(ApplicationService.UpdateApplicationResultCode.INTERNAL_ERROR);
        when(result.record()).thenReturn(null);

        when(applicationService.updateApplication(eq(5), any(ApplicationUpdate.class))).thenReturn(result);

        mvc.perform(patch("/applications/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void updateApplication_returns200_whenOk() throws Exception {
        var req = new ApplicationUpdate();
        req.setRemarks("updated");

        var rec = fullApplicationRecord(5, UUID.randomUUID());
        var result = mock(ApplicationService.UpdateApplicationResult.class);

        when(result.code()).thenReturn(ApplicationService.UpdateApplicationResultCode.OK);
        when(result.record()).thenReturn(rec);

        when(applicationService.updateApplication(eq(5), any(ApplicationUpdate.class))).thenReturn(result);

        mvc.perform(patch("/applications/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    // ---------------------------
    // DELETE
    // ---------------------------

    @Test
    @WithMockUser
    void deleteApplication_returns204_whenDeleted() throws Exception {
        when(applicationService.deleteApplication(9)).thenReturn(true);

        mvc.perform(delete("/applications/9"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteApplication_returns404_whenNotExisting() throws Exception {
        when(applicationService.deleteApplication(9)).thenReturn(false);

        mvc.perform(delete("/applications/9"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------
    // helpers
    // ---------------------------

    private static ApplicationCreate validCreate() {
        var req = new ApplicationCreate();
        req.setUserId(UUID.randomUUID());
        req.setLicenseType(LicenseTypeApiEnum.ETV);
        req.setCadastralReference("1234567AB9999C0001DE");
        req.setRemarks("test");
        return req;
    }

    private static ApplicationRecord fullApplicationRecord(int id, UUID userId) {
        var r = new ApplicationRecord();
        r.setId(id);
        r.setUserId(userId.toString());
        r.setCadastralReference("1234567AB9999C0001DE");
        r.setAppliedAt(LocalDateTime.now().minusDays(1));
        r.setChangedAt(LocalDateTime.now());
        r.setRemarks("test");
        r.setLicenseType(LicenseType.etv);
        r.setApplicationStatus(ApplicationStatus.submitted);
        return r;
    }
}