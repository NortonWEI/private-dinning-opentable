package com.opentable.privatedining.controller;

import com.opentable.privatedining.dto.reporting.OccupancyReportDTO;
import com.opentable.privatedining.mapper.reporting.OccupancyReportMapper;
import com.opentable.privatedining.model.reporting.OccupancyReport;
import com.opentable.privatedining.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reporting")
@Tag(name = "Reporting", description = "Returns a detailed breakdown of occupancy levels throughout given periods")
public class ReportingController {

    private final ReportingService reportService;
    private final OccupancyReportMapper occupancyReportMapper;

    public ReportingController(ReportingService reportService, OccupancyReportMapper occupancyReportMapper) {
        this.reportService = reportService;
        this.occupancyReportMapper = occupancyReportMapper;
    }

    @GetMapping("/occupancy")
    @Operation(summary = "Occupancy report", description = "Retrieve occupancy levels for a given restaurant over a specified period")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Valid id and date/time range",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OccupancyReportDTO.class))),
        @ApiResponse(responseCode = "404", description = "Restaurant/space not found"),
        @ApiResponse(responseCode = "400", description = "Invalid start/end time or granularity")
    })
    public ResponseEntity<OccupancyReportDTO> getOccupancyReport(@RequestBody OccupancyReportDTO requestDto) {
        try {
            OccupancyReport request = occupancyReportMapper.toModel(requestDto);
            Optional<OccupancyReport> report = reportService.getOccupancyReport(request);
            return report.map(r -> ResponseEntity.ok(occupancyReportMapper.toDto(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
