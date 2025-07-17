package com.mobios.analyticsservice.dto.report;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private String fileName;
    private String downloadUrl;
    private String contentType;
    private byte[] content;
    private Long fileSize;
    private String generatedBy;
    private String reportType;
    private String format;
}
