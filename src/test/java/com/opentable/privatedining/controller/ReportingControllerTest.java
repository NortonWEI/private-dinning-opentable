package com.opentable.privatedining.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opentable.privatedining.dto.reporting.OccupancyDataDTO;
import com.opentable.privatedining.dto.reporting.OccupancyReportDTO;
import com.opentable.privatedining.exception.GlobalExceptionHandler;
import com.opentable.privatedining.mapper.reporting.OccupancyReportMapper;
import com.opentable.privatedining.model.reporting.OccupancyData;
import com.opentable.privatedining.model.reporting.OccupancyReport;
import com.opentable.privatedining.service.ReportingService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ReportingController.class, GlobalExceptionHandler.class})
public class ReportingControllerTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportingService reportingService;

    @MockBean
    private OccupancyReportMapper occupancyReportMapper;

    @Test
    void getOccupancyReport_WhenValidRequest_ShouldReturnReport() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId("64b64c4f2f4e4b3a2c8b4567");
        UUID spaceId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2026, 1, 30, 18, 0);
        LocalDateTime end = start.plusHours(2);

        OccupancyReport request = new OccupancyReport(restaurantId, spaceId, start, end, null, null);
        when(occupancyReportMapper.toModel(any())).thenReturn(request);
        Optional<OccupancyReport> returnedReport = Optional.of(new OccupancyReport(restaurantId, spaceId, start, end,
            new OccupancyData(), List.of()));
        when(reportingService.getOccupancyReport(any())).thenReturn(returnedReport);
        OccupancyReportDTO response = new OccupancyReportDTO(restaurantId.toHexString(), spaceId.toString(), start, end,
            new OccupancyDataDTO(), List.of());
        when(occupancyReportMapper.toDto(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(
                get("/v1/reporting/" + restaurantId.toHexString() + "/occupancy").param("start", start.format(FORMATTER))
                    .param("end", end.format(FORMATTER)).param("spaceId", spaceId.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.restaurantId").value(restaurantId.toHexString()))
            .andExpect(jsonPath("$.spaceId").value(spaceId.toString()));
    }

    @Test
    void getOccupancyReport_NoReportFound_ShouldReturn404() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId("64b64c4f2f4e4b3a2c8b4567");
        UUID spaceId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2026, 1, 30, 18, 0);
        LocalDateTime end = start.plusHours(2);

        OccupancyReport request = new OccupancyReport(restaurantId, spaceId, start, end, null, null);
        when(occupancyReportMapper.toModel(any())).thenReturn(request);
        when(reportingService.getOccupancyReport(any())).thenReturn(Optional.empty());
        OccupancyReportDTO response = new OccupancyReportDTO(restaurantId.toHexString(), spaceId.toString(), start, end,
            new OccupancyDataDTO(), List.of());
        when(occupancyReportMapper.toDto(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(
                get("/v1/reporting/" + restaurantId.toHexString() + "/occupancy").param("start", start.format(FORMATTER))
                    .param("end", end.format(FORMATTER)).param("spaceId", spaceId.toString()))
            .andExpect(status().isNotFound());
    }

    @Test
    void getOccupancyReport_WhenValidRequest_ShouldReturn400() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId("64b64c4f2f4e4b3a2c8b4567");
        UUID spaceId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2026, 1, 30, 18, 0);
        LocalDateTime end = start.plusHours(2);

        OccupancyReport request = new OccupancyReport(restaurantId, spaceId, start, end, null, null);
        when(occupancyReportMapper.toModel(any())).thenReturn(request);
        when(reportingService.getOccupancyReport(any())).thenThrow(new IllegalArgumentException("Invalid report"));
        OccupancyReportDTO response = new OccupancyReportDTO(restaurantId.toHexString(), spaceId.toString(), start, end,
            new OccupancyDataDTO(), List.of());
        when(occupancyReportMapper.toDto(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(
                get("/v1/reporting/" + restaurantId.toHexString() + "/occupancy").param("start", start.format(FORMATTER))
                    .param("end", end.format(FORMATTER)).param("spaceId", spaceId.toString()))
            .andExpect(status().isBadRequest());
    }
}
