package de.hft.licensing.application.restController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.application.services.BallotPeriodService;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.model.CreateBallotPeriodRequest;
import de.hft.licensing.model.RunLotteryForBallotPeriodRequest;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BallotPeriodsController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://dummy-issuer"
})
class BallotPeriodsControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean BallotPeriodService ballotPeriodService;

    @Test
    @WithMockUser(roles = "admin")
    void listBallotPeriods_returns200_andTotalApplications() throws Exception {
        var r1 = ballotPeriodRecord(1, LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 10, 0));
        var r2 = ballotPeriodRecord(2, LocalDateTime.of(2026, 2, 1, 10, 0), LocalDateTime.of(2026, 2, 2, 10, 0));

        when(ballotPeriodService.listBallotPeriods()).thenReturn(List.of(r1, r2));
        when(ballotPeriodService.countApplicationsInPeriod(1)).thenReturn(5);
        when(ballotPeriodService.countApplicationsInPeriod(2)).thenReturn(7);

        var res = mvc.perform(get("/ballot-periods"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString());
        assertTrue(root.isArray());
        assertEquals(2, root.size());

        assertTrue(root.get(0).has("totalApplications"));
        assertEquals(5, root.get(0).get("totalApplications").asInt());
        assertEquals(7, root.get(1).get("totalApplications").asInt());
    }

    @Test
    @WithMockUser(roles = "admin")
    void createBallotPeriod_returns400_whenInvalid() throws Exception {
        var req = new CreateBallotPeriodRequest();
        req.setStartDate(OffsetDateTime.parse("2026-01-02T10:00:00Z"));
        req.setEndDate(OffsetDateTime.parse("2026-01-01T10:00:00Z"));

        mvc.perform(post("/ballot-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin")
    void createBallotPeriod_returns409_whenOverlaps() throws Exception {
        var req = new CreateBallotPeriodRequest();
        req.setStartDate(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        req.setEndDate(OffsetDateTime.parse("2026-01-02T10:00:00Z"));

        var result = mock(BallotPeriodService.CreateBallotPeriodResult.class);
        when(result.code()).thenReturn(BallotPeriodService.CreateBallotPeriodResultCode.OVERLAPS);
        when(result.record()).thenReturn(null);

        when(ballotPeriodService.createBallotPeriod(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(result);

        mvc.perform(post("/ballot-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "admin")
    void createBallotPeriod_returns500_whenInternalErrorOrRecordNull() throws Exception {
        var req = new CreateBallotPeriodRequest();
        req.setStartDate(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        req.setEndDate(OffsetDateTime.parse("2026-01-02T10:00:00Z"));

        var result = mock(BallotPeriodService.CreateBallotPeriodResult.class);
        when(result.code()).thenReturn(BallotPeriodService.CreateBallotPeriodResultCode.INTERNAL_ERROR);
        when(result.record()).thenReturn(null);

        when(ballotPeriodService.createBallotPeriod(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(result);

        mvc.perform(post("/ballot-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "admin")
    void createBallotPeriod_returns200_whenOk() throws Exception {
        var req = new CreateBallotPeriodRequest();
        req.setStartDate(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        req.setEndDate(OffsetDateTime.parse("2026-01-02T10:00:00Z"));

        var record = ballotPeriodRecord(7, LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 10, 0));

        var result = mock(BallotPeriodService.CreateBallotPeriodResult.class);
        when(result.code()).thenReturn(BallotPeriodService.CreateBallotPeriodResultCode.OK);
        when(result.record()).thenReturn(record);

        when(ballotPeriodService.createBallotPeriod(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(result);

        mvc.perform(post("/ballot-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void getBallotPeriod_returns204_whenNone() throws Exception {
        when(ballotPeriodService.getActiveBallotPeriodUtcNow()).thenReturn(null);

        mvc.perform(get("/ballot-periods/current"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getBallotPeriod_returns200_whenPresent() throws Exception {
        var record = ballotPeriodRecord(3,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 2, 10, 0));

        when(ballotPeriodService.getActiveBallotPeriodUtcNow()).thenReturn(record);

        mvc.perform(get("/ballot-periods/current"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "admin")
    void getBallotPeriodDetails_returns400_whenInvalidId() throws Exception {
        mvc.perform(get("/ballot-periods/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin")
    void getBallotPeriodDetails_returns404_whenNotFound() throws Exception {
        when(ballotPeriodService.getBallotPeriodById(5)).thenReturn(null);

        mvc.perform(get("/ballot-periods/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "admin")
    void getBallotPeriodDetails_returns200_whenFound() throws Exception {
        when(ballotPeriodService.getBallotPeriodById(5))
                .thenReturn(ballotPeriodRecord(5, LocalDateTime.of(2026, 3, 1, 10, 0), LocalDateTime.of(2026, 3, 2, 10, 0)));

        mvc.perform(get("/ballot-periods/5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "admin")
    void getBallotPeriodEntries_returns200() throws Exception {
        var a1 = applicationRecord(1);

        when(ballotPeriodService.getBallotPeriodEntries(5)).thenReturn(List.of(a1));

        mvc.perform(get("/ballot-periods/5/entries"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "admin")
    void runLottery_returns400_whenPeriodIdInvalid() throws Exception {
        mvc.perform(post("/ballot-periods/0/lottery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin")
    void runLottery_returns404_whenNotFound() throws Exception {
        var result = mock(BallotPeriodService.RunLotteryResult.class);
        when(result.code()).thenReturn(BallotPeriodService.RunLotteryResultCode.NOT_FOUND);

        when(ballotPeriodService.runLotteryForBallotPeriod(eq(5), any(), any())).thenReturn(result);

        mvc.perform(post("/ballot-periods/5/lottery")
                        .queryParam("licensesCount", "3")
                        .queryParam("licenses_to_distribute", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "admin")
    void runLottery_returns400_whenBadRequest() throws Exception {
        var result = mock(BallotPeriodService.RunLotteryResult.class);
        when(result.code()).thenReturn(BallotPeriodService.RunLotteryResultCode.BAD_REQUEST);

        when(ballotPeriodService.runLotteryForBallotPeriod(eq(5), any(), any())).thenReturn(result);

        mvc.perform(post("/ballot-periods/5/lottery")
                        .queryParam("licensesCount", "3")
                        .queryParam("licenses_to_distribute", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin")
    void runLottery_returns500_whenInternalErrorOrListsNull() throws Exception {
        var result = mock(BallotPeriodService.RunLotteryResult.class);
        when(result.code()).thenReturn(BallotPeriodService.RunLotteryResultCode.INTERNAL_ERROR);
        when(result.selected()).thenReturn(null);
        when(result.notSelected()).thenReturn(null);

        when(ballotPeriodService.runLotteryForBallotPeriod(eq(5), any(), any())).thenReturn(result);

        mvc.perform(post("/ballot-periods/5/lottery")
                        .queryParam("licensesCount", "3")
                        .queryParam("licenses_to_distribute", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "admin")
    void runLottery_returns200_whenOk() throws Exception {
        var selected = List.of(applicationRecord(1));
        var notSelected = List.of(applicationRecord(2));

        var result = mock(BallotPeriodService.RunLotteryResult.class);
        when(result.code()).thenReturn(BallotPeriodService.RunLotteryResultCode.OK);
        when(result.selected()).thenReturn(selected);
        when(result.notSelected()).thenReturn(notSelected);

        when(ballotPeriodService.runLotteryForBallotPeriod(eq(5), any(), any())).thenReturn(result);

        var body = new RunLotteryForBallotPeriodRequest();
        setEnumOrString(body, "setLicenseType", "ETV");

        var res = mvc.perform(post("/ballot-periods/5/lottery")
                        .queryParam("licensesCount", "3")
                        .queryParam("licenses_to_distribute", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString());
        assertTrue(root.has("selected_applications") || root.has("selectedApplications"));
        assertTrue(root.has("not_selected_applications") || root.has("notSelectedApplications"));
    }

    private static BallotPeriodRecord ballotPeriodRecord(int id, LocalDateTime start, LocalDateTime end) {
        var r = new BallotPeriodRecord();
        r.setId(id);
        r.setStartDate(start);
        r.setEndDate(end);
        return r;
    }

    private static ApplicationRecord applicationRecord(int id) {
        var r = new ApplicationRecord();
        r.setId(id);
        r.setUserId(UUID.randomUUID().toString());
        r.setCadastralReference("1234567AB9999C0001DE");
        r.setAppliedAt(LocalDateTime.now().minusDays(1));
        r.setChangedAt(LocalDateTime.now());
        r.setRemarks("x");
        setEnumOrString(r, "setLicenseType", "ETV");
        setEnumOrString(r, "setApplicationStatus", "SUBMITTED");
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
}