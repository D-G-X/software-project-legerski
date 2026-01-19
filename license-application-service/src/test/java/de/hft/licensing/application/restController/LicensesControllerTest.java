package de.hft.licensing.application.restController;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.application.services.LicenseService;
import de.hft.licensing.application.services.authServices.LicenseAuthorizationService;
import de.hft.licensing.db.tables.records.LicenseRecord;
import de.hft.licensing.model.UpdateLicenseStatusRequest;
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

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LicensesController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://dummy-issuer"
})
class LicensesControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean LicenseService licenseService;

    @MockitoBean(name = "licenseAuthorization")
    LicenseAuthorizationService licenseAuthorization;

    @BeforeEach
    void allowAuthorization() {
        lenient().when(licenseAuthorization.canAccessLicense(any(), any())).thenReturn(true);
        lenient().when(licenseAuthorization.canListLicenses(any(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser
    void deleteLicense_returns400_whenInvalidId() throws Exception {
        mvc.perform(delete("/licenses/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deleteLicense_returns204_whenDeleted() throws Exception {
        when(licenseService.deleteLicense(7)).thenReturn(true);

        mvc.perform(delete("/licenses/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteLicense_returns404_whenNotDeleted() throws Exception {
        when(licenseService.deleteLicense(7)).thenReturn(false);

        mvc.perform(delete("/licenses/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getLicense_returns400_whenInvalidId() throws Exception {
        mvc.perform(get("/licenses/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getLicense_returns404_whenServiceReturnsNull() throws Exception {
        when(licenseService.getLicenseWithCadastral(7)).thenReturn(null);

        mvc.perform(get("/licenses/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getLicense_returns404_whenJoinedLicenseNull() throws Exception {
        Object joined = mockJoined(returnTypeOfGetLicenseWithCadastral(), null, null);
        doReturn(joined).when(licenseService).getLicenseWithCadastral(7);

        mvc.perform(get("/licenses/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getLicense_returns200_whenOk_andSetsCadastralReference() throws Exception {
        UUID userId = UUID.randomUUID();
        LicenseRecord rec = licenseRecord(7, userId);

        Object joined = mockJoined(returnTypeOfGetLicenseWithCadastral(), rec, "1234567AB9999C0001DE");
        doReturn(joined).when(licenseService).getLicenseWithCadastral(7);

        mvc.perform(get("/licenses/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.cadastral_reference").value("1234567AB9999C0001DE"));
    }

    @Test
    @WithMockUser
    void updateLicenseStatus_returns400_whenInvalid() throws Exception {
        mvc.perform(patch("/licenses/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateLicenseStatus_returns404_whenServiceReturnsNull() throws Exception {
        var req = new UpdateLicenseStatusRequest();
        setEnumOrString(req, "setLicenseStatus", "ACTIVE");

        when(licenseService.updateLicenseStatus(eq(7), any(UpdateLicenseStatusRequest.class))).thenReturn(null);

        mvc.perform(patch("/licenses/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateLicenseStatus_returns200_whenOk() throws Exception {
        var req = new UpdateLicenseStatusRequest();
        setEnumOrString(req, "setLicenseStatus", "ACTIVE");

        UUID userId = UUID.randomUUID();
        LicenseRecord updated = licenseRecord(7, userId);

        when(licenseService.updateLicenseStatus(eq(7), any(UpdateLicenseStatusRequest.class))).thenReturn(updated);

        mvc.perform(patch("/licenses/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void listLicenses_returns401_whenAuthenticationNotJwt() throws Exception {
        mvc.perform(get("/licenses"))
                .andExpect(status().isUnauthorized());
    }


    private static LicenseRecord licenseRecord(int id, UUID userId) {
        var r = new LicenseRecord();
        r.setId(id);
        r.setUserId(userId.toString());
        r.setApplicationId(100 + id);
        r.setIssuedAt(LocalDateTime.now().minusDays(10));
        r.setExpiresAt(LocalDateTime.now().plusDays(10));
        setEnumOrString(r, "setLicenseType", "ETV");
        setEnumOrString(r, "setLicenseStatus", "ACTIVE");
        return r;
    }

    private static void setEnumOrString(Object target, String setterName, String value) {
        try {
            Method m = null;
            for (var mm : target.getClass().getMethods()) {
                if (mm.getName().equals(setterName) && mm.getParameterCount() == 1) {
                    m = mm;
                    break;
                }
            }
            if (m == null) return;

            Class<?> p = m.getParameterTypes()[0];
            Object arg = null;

            if (p == String.class) {
                arg = value;
            } else if (p.isEnum()) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Class<? extends Enum> ec = (Class<? extends Enum>) p;
                try {
                    arg = Enum.valueOf(ec, value);
                } catch (IllegalArgumentException e) {
                    arg = Enum.valueOf(ec, value.toLowerCase());
                }
            }

            if (arg != null) {
                m.invoke(target, arg);
            }
        } catch (Exception ignored) {
        }
    }

    private Class<?> returnTypeOfGetLicenseWithCadastral() {
        try {
            return LicenseService.class.getMethod("getLicenseWithCadastral", Integer.class).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> returnElementTypeOfListLicensesWithCadastral() {
        try {
            return LicenseService.class.getMethod("listLicensesWithCadastral", UUID.class).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object mockJoined(Class<?> clazz, LicenseRecord license, String cadastralReference) {
        return mock((Class) clazz, invocation -> {
            String name = invocation.getMethod().getName();
            if (name.equals("license")) return license;
            if (name.equals("cadastralReference")) return cadastralReference;
            return RETURNS_DEFAULTS.answer(invocation);
        });
    }
}