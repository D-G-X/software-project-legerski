package de.hft.licensing.application.restController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReactForwardController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://dummy-issuer"
})
class ReactForwardControllerWebMvcTest {

    @Autowired MockMvc mvc;

    @Test
    void forwards_forNonBackendPaths_withoutDot() throws Exception {
        mvc.perform(get("/app/settings"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(forwardedUrl("/"));
    }

    @Test
    void doesNotMatch_pathsStartingWithApi() throws Exception {
        mvc.perform(get("/api/test"))
                .andExpect(status().isNotFound());
    }

    @Test
    void doesNotMatch_pathsStartingWithSwagger() throws Exception {
        mvc.perform(get("/swagger"))
                .andExpect(status().isNotFound());
    }

    @Test
    void doesNotMatch_staticPrefixes() throws Exception {
        mvc.perform(get("/css/main.css"))
                .andExpect(status().isNotFound());

        mvc.perform(get("/js/app.js"))
                .andExpect(status().isNotFound());

        mvc.perform(get("/images/logo.png"))
                .andExpect(status().isNotFound());

        mvc.perform(get("/public/anything"))
                .andExpect(status().isNotFound());
    }

}