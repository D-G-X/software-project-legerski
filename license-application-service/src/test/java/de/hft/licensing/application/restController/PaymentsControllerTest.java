package de.hft.licensing.application.restController;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.application.services.PaymentService;
import de.hft.licensing.application.services.authServices.PaymentAuthorizationService;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.model.ApplicationPaymentCreate;
import de.hft.licensing.utils.ApiFormValidator;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentsController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://dummy-issuer"
})
class PaymentsControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean PaymentService paymentService;

    @MockitoBean ApiFormValidator formValidator;

    @MockitoBean(name = "paymentAuthorization")
    PaymentAuthorizationService paymentAuthorization;

    @BeforeEach
    void allowAuthzAndDefaultValidation() {
        lenient().when(paymentAuthorization.canAccessPayments(any(), any())).thenReturn(true);
        lenient().when(formValidator.isValidName(anyString())).thenReturn(true);
        lenient().when(formValidator.isValidIban(anyString())).thenReturn(true);
        lenient().when(formValidator.isValidBic(anyString())).thenReturn(true);
    }

    @Test
    @WithMockUser
    void createPayment_returns400_whenMissingParams() throws Exception {
        mvc.perform(post("/applications/5/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        var req = new ApplicationPaymentCreate();
        req.setName("A");
        req.setIban("DE00...");
        req.setBic("BIC...");
        req.setApplicationId(null);

        mvc.perform(post("/applications/5/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createPayment_returns400_whenValidationFails() throws Exception {
        var req = new ApplicationPaymentCreate();
        req.setApplicationId(5);
        req.setName("bad");
        req.setIban("bad");
        req.setBic("bad");

        when(formValidator.isValidName("bad")).thenReturn(false);
        when(formValidator.isValidIban("bad")).thenReturn(false);
        when(formValidator.isValidBic("bad")).thenReturn(false);

        mvc.perform(post("/applications/5/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser
    void createPayment_returns500_whenServiceReturnsNull() throws Exception {
        var req = new ApplicationPaymentCreate();
        req.setApplicationId(5);
        req.setName("Max");
        req.setIban("DE02120300000000202051");
        req.setBic("BYLADEM1001");

        when(paymentService.createPayment(eq(5), any(ApplicationPaymentCreate.class))).thenReturn(null);

        mvc.perform(post("/applications/5/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void createPayment_returns201_whenOk() throws Exception {
        var req = new ApplicationPaymentCreate();
        req.setApplicationId(5);
        req.setName("Max");
        req.setIban("DE02120300000000202051");
        req.setBic("BYLADEM1001");

        var rec = new ApplicationPaymentRecord();
        rec.setId(9);
        rec.setApplicationId(5);
        rec.setAmount(new BigDecimal("123.45"));
        rec.setPaymentDate(LocalDateTime.now().minusDays(1));
        setEnumOrString(rec, "setPaymentStatus", "PAID");
        rec.setAccountant("Max");
        rec.setIban(req.getIban());
        rec.setBic(req.getBic());

        when(paymentService.createPayment(eq(5), any(ApplicationPaymentCreate.class))).thenReturn(rec);

        mvc.perform(post("/applications/5/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/applications/5/payments/9"))
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.application_id").value(5));
    }

    @Test
    @WithMockUser
    void listPayments_returns400_whenIdNull_notPossibleOverHttp_butInvalidPathGives404() throws Exception {
        mvc.perform(get("/applications//payments"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void listPayments_returns404_whenServiceReturnsNull() throws Exception {
        when(paymentService.listPayments(5)).thenReturn(null);

        mvc.perform(get("/applications/5/payments"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void listPayments_returns200_whenOk() throws Exception {
        var rec = new ApplicationPaymentRecord();
        rec.setId(1);
        rec.setApplicationId(5);
        rec.setAmount(new BigDecimal("10.00"));
        rec.setPaymentDate(LocalDateTime.now().minusDays(1));
        setEnumOrString(rec, "setPaymentStatus", "PAID");
        rec.setAccountant("Max");
        rec.setIban("DE02120300000000202051");
        rec.setBic("BYLADEM1001");

        when(paymentService.listPayments(5)).thenReturn(List.of(rec));

        mvc.perform(get("/applications/5/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].application_id").value(5));
    }

    @Test
    void getApplicationFee_returns400_whenIdNull_notPossibleOverHttp_butInvalidPathGives404() throws Exception {
        mvc.perform(get("/fees/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getApplicationFee_returns404_whenApplicationNotFound() throws Exception {
        when(paymentService.getApplication(5)).thenReturn(null);

        mvc.perform(get("/fees/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getApplicationFee_returns200_andCalculatesFeesForEachLicenseType() throws Exception {
        var app = new ApplicationRecord();
        app.setId(5);
        app.setUserId(UUID.randomUUID().toString());

        app.setLicenseType(LicenseType.etv);
        when(paymentService.getApplication(5)).thenReturn(app);
        mvc.perform(get("/fees/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application_id").value(5))
                .andExpect(jsonPath("$.fee_amount").value(3500));

        app.setLicenseType(LicenseType.etvpl);
        when(paymentService.getApplication(5)).thenReturn(app);
        mvc.perform(get("/fees/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fee_amount").value(875));

        app.setLicenseType(LicenseType.etv60);
        when(paymentService.getApplication(5)).thenReturn(app);
        mvc.perform(get("/fees/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fee_amount").value(290));
    }

    private static void setEnumOrString(Object target, String setterName, String value) {
        try {
            java.lang.reflect.Method m = null;
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
}