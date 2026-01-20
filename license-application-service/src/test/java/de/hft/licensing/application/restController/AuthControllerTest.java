package de.hft.licensing.application.restController;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.application.services.AuthService;
import de.hft.licensing.application.services.authServices.AuthenticationAuthorizationService;
import de.hft.licensing.model.*;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://dummy-issuer"
})
class AuthControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;

    @MockitoBean(name = "authenticationAuthorization")
    AuthenticationAuthorizationService authenticationAuthorization;

    @BeforeEach
    void allowAuthorization() {
        lenient().when(authenticationAuthorization.userIsUserOrAdmin(any(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser
    void loginUser_returns200_whenServiceReturnsToken() throws Exception {
        var req = new LoginRequest();
        req.setEmail("a@b.de");
        req.setPassword("pw");

        var res = new LoginResource();
        res.setAccessToken("acc");
        res.setRefreshToken("ref");
        res.setTokenType("Bearer");
        res.setExpiresIn(3600);

        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("acc"));
    }

    @Test
    @WithMockUser
    void loginUser_returns400_whenInvalidEmailAndPassword() throws Exception {
        var req = new LoginRequest();
        req.setEmail("not-an-email");
        req.setPassword("123");

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @WithMockUser
    void refreshLogin_returns200() throws Exception {
        var req = new RefreshLoginRequest();
        req.setRefreshToken("ref");

        var res = new LoginResource();
        res.setAccessToken("new");
        res.setRefreshToken("ref2");
        res.setTokenType("Bearer");
        res.setExpiresIn(3600);

        when(authService.refreshLogin(any(RefreshLoginRequest.class))).thenReturn(res);

        mvc.perform(post("/refresh-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new"));
    }

    @Test
    @WithMockUser
    void registerUser_returns409_whenServiceReturnsNull() throws Exception {
        var req = new RegisterRequest();
        req.setFirstname("Max");
        req.setLastname("Mustermann");
        req.setEmail("max@uni.de");
        req.setPassword("StrongPass123!");

        when(authService.register(any(RegisterRequest.class))).thenReturn(null);

        mvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void registerUser_returns400_whenUserIdNull() throws Exception {
        var req = new RegisterRequest();
        req.setFirstname("Max");
        req.setLastname("Mustermann");
        req.setEmail("max@uni.de");
        req.setPassword("StrongPass123!");

        var res = new RegisterResource();
        res.setUserId(null);

        when(authService.register(any(RegisterRequest.class))).thenReturn(res);

        mvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void registerUser_returns201_whenOk() throws Exception {
        var req = new RegisterRequest();
        req.setFirstname("Max");
        req.setLastname("Mustermann");
        req.setEmail("max@uni.de");
        req.setPassword("StrongPass123!");

        var id = UUID.randomUUID();
        var res = new RegisterResource();
        res.setUserId(id);

        when(authService.register(any(RegisterRequest.class))).thenReturn(res);

        mvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser
    void registerUser_returns400_whenAllInvalid_andServiceNotNull() throws Exception {
        var req = new RegisterRequest();
        req.setFirstname("1");
        req.setLastname("2");
        req.setEmail("nope");
        req.setPassword("123");

        var res = new RegisterResource();
        res.setUserId(UUID.randomUUID());

        when(authService.register(any(RegisterRequest.class))).thenReturn(res);

        mvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void requestPasswordReset_returns400_whenBodyNull() throws Exception {
        mvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void requestPasswordReset_returns400_whenEmailMissing() throws Exception {
        mvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void requestPasswordReset_returns404_whenNotFound() throws Exception {
        var req = new ResetPasswordRequest();
        req.setEmail("a@b.de");

        when(authService.requestPasswordReset("a@b.de"))
                .thenReturn(AuthService.RequestPasswordResetResult.NOT_FOUND);

        mvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void requestPasswordReset_returns500_whenInternalError() throws Exception {
        var req = new ResetPasswordRequest();
        req.setEmail("a@b.de");

        when(authService.requestPasswordReset("a@b.de"))
                .thenReturn(AuthService.RequestPasswordResetResult.INTERNAL_ERROR);

        mvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void requestPasswordReset_returns200_whenOk() throws Exception {
        var req = new ResetPasswordRequest();
        req.setEmail("a@b.de");

        when(authService.requestPasswordReset("a@b.de"))
                .thenReturn(AuthService.RequestPasswordResetResult.OK);

        mvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void changeUserDetails_returns400_whenBodyNull() throws Exception {
        var userId = UUID.randomUUID();

        mvc.perform(post("/change-userdetails/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changeUserDetails_returns409_whenConflict() throws Exception {
        var userId = UUID.randomUUID();
        var req = new ChangeUserdetailsRequest();
        req.setEmail("new@x.de");

        when(authService.changeUserDetails(eq(userId), any(ChangeUserdetailsRequest.class)))
                .thenReturn(AuthService.ChangeUserDetailsResult.CONFLICT);

        mvc.perform(post("/change-userdetails/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void changeUserDetails_returns400_whenBadRequest() throws Exception {
        var userId = UUID.randomUUID();
        var req = new ChangeUserdetailsRequest();
        req.setEmail("nope");

        when(authService.changeUserDetails(eq(userId), any(ChangeUserdetailsRequest.class)))
                .thenReturn(AuthService.ChangeUserDetailsResult.BAD_REQUEST);

        mvc.perform(post("/change-userdetails/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changeUserDetails_returns400_whenFailed() throws Exception {
        var userId = UUID.randomUUID();
        var req = new ChangeUserdetailsRequest();
        req.setEmail("x@y.de");

        when(authService.changeUserDetails(eq(userId), any(ChangeUserdetailsRequest.class)))
                .thenReturn(AuthService.ChangeUserDetailsResult.FAILED);

        mvc.perform(post("/change-userdetails/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changeUserDetails_returns200_whenOk() throws Exception {
        var userId = UUID.randomUUID();
        var req = new ChangeUserdetailsRequest();
        req.setEmail("x@y.de");

        when(authService.changeUserDetails(eq(userId), any(ChangeUserdetailsRequest.class)))
                .thenReturn(AuthService.ChangeUserDetailsResult.OK);

        mvc.perform(post("/change-userdetails/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void changeUserPassword_returns400_whenTokenMissing() throws Exception {
        var req = new ChangePasswordRequest();
        req.setNewPassword("StrongPass123!");

        mvc.perform(post("/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changeUserPassword_returns400_whenBadRequestFromService() throws Exception {
        var req = new ChangePasswordRequest();
        req.setToken("tok");
        req.setNewPassword("StrongPass123!");

        when(authService.changeUserPassword("tok", "StrongPass123!"))
                .thenReturn(AuthService.ChangePasswordResult.BAD_REQUEST);

        mvc.perform(post("/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changeUserPassword_returns500_whenInternalError() throws Exception {
        var req = new ChangePasswordRequest();
        req.setToken("tok");
        req.setNewPassword("StrongPass123!");

        when(authService.changeUserPassword("tok", "StrongPass123!"))
                .thenReturn(AuthService.ChangePasswordResult.INTERNAL_ERROR);

        mvc.perform(post("/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void changeUserPassword_returns200_whenOk() throws Exception {
        var req = new ChangePasswordRequest();
        req.setToken("tok");
        req.setNewPassword("StrongPass123!");

        when(authService.changeUserPassword("tok", "StrongPass123!"))
                .thenReturn(AuthService.ChangePasswordResult.OK);

        mvc.perform(post("/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}