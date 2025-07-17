package com.mobios.analyticsservice.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    private String reportType; // "all", "by_file", "by_user", "by_date_range"
    private String format; // "pdf", "csv", "excel"
    private String fileName;
    private String processedBy;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> fileNames;
    private Boolean includeInvalid = true;
}
