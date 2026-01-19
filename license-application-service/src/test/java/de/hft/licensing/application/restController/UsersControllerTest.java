package de.hft.licensing.application.restController;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.application.services.UserService;
import de.hft.licensing.application.services.authServices.PreferencesAuthorizationService;
import de.hft.licensing.application.services.authServices.UserAuthorizationService;
import de.hft.licensing.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://dummy-issuer"
})
class UsersControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserService userService;

    @MockitoBean(name = "userAuthorization")
    UserAuthorizationService userAuthorization;

    @MockitoBean(name = "preferencesAuthorization")
    PreferencesAuthorizationService preferencesAuthorization;

    @BeforeEach
    void allowAuthz() {
        lenient().when(userAuthorization.canAccessUser(any(), any())).thenReturn(true);
        lenient().when(preferencesAuthorization.canAccessPreferences(any(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser(roles = "admin")
    void createUser_returns400_whenInvalid() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin")
    void createUser_returns201_whenOk() throws Exception {
        var req = new CreateUserRequest();
        var schema = new UserCreate();
        schema.setUsername("john");
        req.setSchema(schema);

        when(userService.createUserWithInitialPreferences()).thenReturn("abc");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/abc"));
    }

    @Test
    @WithMockUser(roles = "admin")
    void createUser_returns409_whenDataIntegrityViolation() throws Exception {
        var req = new CreateUserRequest();
        var schema = new UserCreate();
        schema.setUsername("john");
        req.setSchema(schema);

        when(userService.createUserWithInitialPreferences()).thenThrow(new DataIntegrityViolationException("x"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "admin")
    void createUser_returns500_whenRuntimeException() throws Exception {
        var req = new CreateUserRequest();
        var schema = new UserCreate();
        schema.setUsername("john");
        req.setSchema(schema);

        when(userService.createUserWithInitialPreferences()).thenThrow(new RuntimeException("x"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void deleteUser_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenReturn(UserService.DeleteUserResult.NOT_FOUND);

        mvc.perform(delete("/users/{userId}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteUser_returns502_whenKeycloakError() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenReturn(UserService.DeleteUserResult.KEYCLOAK_ERROR);

        mvc.perform(delete("/users/{userId}", id))
                .andExpect(status().isBadGateway());
    }

    @Test
    @WithMockUser
    void deleteUser_returns500_whenInternalError() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenReturn(UserService.DeleteUserResult.INTERNAL_ERROR);

        mvc.perform(delete("/users/{userId}", id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void deleteUser_returns204_whenOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenReturn(UserService.DeleteUserResult.OK);

        mvc.perform(delete("/users/{userId}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void getUser_returns404_whenNull() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUser(id)).thenReturn(null);

        mvc.perform(get("/users/{userId}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getUser_returns200_whenOk() throws Exception {
        UUID id = UUID.randomUUID();
        var u = new UserResource();
        u.setId(id);
        when(userService.getUser(id)).thenReturn(u);

        mvc.perform(get("/users/{userId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "admin")
    void listUsers_appliesPaginationDefaults_andCapsMax100() throws Exception {
        when(userService.listUsers(eq(0), eq(100))).thenReturn(List.of());

        mvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userService).listUsers(0, 100);

        when(userService.listUsers(eq(5), eq(500))).thenReturn(List.of());

        mvc.perform(get("/users")
                        .queryParam("first", "5")
                        .queryParam("max", "500"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin")
    void updateUser_returns400_whenInvalid() throws Exception {
        mvc.perform(patch("/users/{userId}", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        UUID id = UUID.randomUUID();
        mvc.perform(patch("/users/{userId}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin")
    void updateUser_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(eq(id), any(UpdateUserRequest.class))).thenReturn(UserService.UpdateUserResult.NOT_FOUND);

        mvc.perform(patch("/users/{userId}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "admin")
    void updateUser_returns204_whenOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(eq(id), any(UpdateUserRequest.class))).thenReturn(UserService.UpdateUserResult.OK);

        mvc.perform(patch("/users/{userId}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "admin")
    void updateUser_returns500_whenFailedResult() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(eq(id), any(UpdateUserRequest.class))).thenReturn(UserService.UpdateUserResult.INTERNAL_ERROR);

        mvc.perform(patch("/users/{userId}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "admin")
    void updateUser_returnsStatusFromRestClientResponseException() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(eq(id), any(UpdateUserRequest.class)))
                .thenThrow(new RestClientResponseException("kc", 503, "Service Unavailable", null, null, StandardCharsets.UTF_8));

        mvc.perform(patch("/users/{userId}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @WithMockUser
    void getNotifications_returns404_whenNull() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getNotifications(userId)).thenReturn(null);

        mvc.perform(get("/users/{userId}/notifications", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getNotifications_returns200_whenOk() throws Exception {
        UUID userId = UUID.randomUUID();
        var n = new UserNotificationResource();
        n.setId(UUID.randomUUID());
        n.setUserId(userId);
        n.setMessage("x");
        when(userService.getNotifications(userId)).thenReturn(List.of(n));

        mvc.perform(get("/users/{userId}/notifications", userId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updateNotification_returns404_whenNotUpdated() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.markNotificationRead(id)).thenReturn(false);

        mvc.perform(patch("/users/notifications/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateNotification_returns200_whenOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.markNotificationRead(id)).thenReturn(true);

        mvc.perform(patch("/users/notifications/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getNotificationPreferences_returns404_whenNull() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getNotificationPreferences(userId)).thenReturn(null);

        mvc.perform(get("/users/{userId}/notifications/preferences", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getNotificationPreferences_returns200_whenOk() throws Exception {
        UUID userId = UUID.randomUUID();
        var prefs = new NotificationPreferencesResource();
        prefs.setUserId(userId);
        setEnumOrString(prefs, "setNotificationWay", "EMAIL");
        prefs.setApplicationUpdatesNotification(true);
        prefs.setLicenseRenewalNotification(false);

        when(userService.getNotificationPreferences(userId)).thenReturn(prefs);

        mvc.perform(get("/users/{userId}/notifications/preferences", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId.toString()));
    }

    @Test
    @WithMockUser
    void updateNotificationPreferences_returns400_whenInvalid() throws Exception {
        UUID userId = UUID.randomUUID();
        mvc.perform(patch("/users/{userId}/notifications/preferences", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/users/{userId}/notifications/preferences", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateNotificationPreferences_returns404_whenNull() throws Exception {
        UUID userId = UUID.randomUUID();
        var update = new NotificationPreferencesUpdate();
        setEnumOrString(update, "setNotificationWay", "EMAIL");

        when(userService.updateNotificationPreferences(eq(userId), any(NotificationPreferencesUpdate.class))).thenReturn(null);

        mvc.perform(patch("/users/{userId}/notifications/preferences", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateNotificationPreferences_returns200_whenOk() throws Exception {
        UUID userId = UUID.randomUUID();
        var update = new NotificationPreferencesUpdate();
        setEnumOrString(update, "setNotificationWay", "EMAIL");

        var prefs = new NotificationPreferencesResource();
        prefs.setUserId(userId);
        setEnumOrString(prefs, "setNotificationWay", "EMAIL");
        prefs.setApplicationUpdatesNotification(true);
        prefs.setLicenseRenewalNotification(false);

        when(userService.updateNotificationPreferences(eq(userId), any(NotificationPreferencesUpdate.class))).thenReturn(prefs);

        mvc.perform(patch("/users/{userId}/notifications/preferences", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId.toString()));
    }

    @Test
    @WithMockUser
    void changePasswordForUser_returns400_whenInvalid() throws Exception {
        UUID userId = UUID.randomUUID();
        mvc.perform(post("/users/{userId}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/users/{userId}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changePasswordForUser_returns404_whenUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        var req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        when(userService.changePassword(eq(userId), any(ChangePasswordForUserRequest.class)))
                .thenReturn(UserService.ChangePasswordResult.USER_NOT_FOUND);

        mvc.perform(post("/users/{userId}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void changePasswordForUser_returns403_whenOldPasswordWrong() throws Exception {
        UUID userId = UUID.randomUUID();
        var req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        when(userService.changePassword(eq(userId), any(ChangePasswordForUserRequest.class)))
                .thenReturn(UserService.ChangePasswordResult.OLD_PASSWORD_WRONG);

        mvc.perform(post("/users/{userId}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void changePasswordForUser_returns400_whenNewPasswordInvalid() throws Exception {
        UUID userId = UUID.randomUUID();
        var req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("bad");

        when(userService.changePassword(eq(userId), any(ChangePasswordForUserRequest.class)))
                .thenReturn(UserService.ChangePasswordResult.NEW_PASSWORD_INVALID);

        mvc.perform(post("/users/{userId}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changePasswordForUser_returns404_whenKeycloakNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        var req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        when(userService.changePassword(eq(userId), any(ChangePasswordForUserRequest.class)))
                .thenReturn(UserService.ChangePasswordResult.KEYCLOAK_NOT_FOUND);

        mvc.perform(post("/users/{userId}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void changePasswordForUser_returns200_whenOk() throws Exception {
        UUID userId = UUID.randomUUID();
        var req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        when(userService.changePassword(eq(userId), any(ChangePasswordForUserRequest.class)))
                .thenReturn(UserService.ChangePasswordResult.OK);

        mvc.perform(post("/users/{userId}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    private static void setEnumOrString(Object target, String setterName, String value) {
        try {
            var methods = target.getClass().getMethods();
            java.lang.reflect.Method m = null;
            for (var mm : methods) {
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

            if (arg != null) m.invoke(target, arg);
        } catch (Exception ignored) {
        }
    }
}