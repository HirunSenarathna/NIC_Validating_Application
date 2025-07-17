package com.mobios.analyticsservice.controller;

import com.mobios.analyticsservice.dto.dashboard.DashboardSummary;
import com.mobios.analyticsservice.dto.report.ReportRequest;
import com.mobios.analyticsservice.dto.report.ReportResponse;
import com.mobios.analyticsservice.service.DashboardService;
import com.mobios.analyticsservice.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j

public class AnalyticsController {

    private final DashboardService dashboardService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        try {
            DashboardSummary summary = dashboardService.getDashboardSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error retrieving dashboard summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reports/generate")
    public ResponseEntity<ByteArrayResource> generateReport(@Valid @RequestBody ReportRequest request) {
        try {
            ReportResponse response = reportService.generateReport(request);

            ByteArrayResource resource = new ByteArrayResource(response.getContent());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + response.getFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, response.getContentType());
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(response.getFileSize()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(response.getFileSize())
                    .contentType(MediaType.parseMediaType(response.getContentType()))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error generating report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reports/preview")
    public ResponseEntity<ReportResponse> previewReport(@Valid @RequestBody ReportRequest request) {
        try {
            // Limit preview to first 100 records
            ReportResponse response = reportService.generateReport(request);

            // Remove actual content for preview, just return metadata
            ReportResponse preview = new ReportResponse();
            preview.setFileName(response.getFileName());
            preview.setContentType(response.getContentType());
            preview.setFileSize(response.getFileSize());
            preview.setGeneratedBy(response.getGeneratedBy());
            preview.setReportType(response.getReportType());
            preview.setFormat(response.getFormat());

            return ResponseEntity.ok(preview);

        } catch (Exception e) {
            log.error("Error previewing report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
