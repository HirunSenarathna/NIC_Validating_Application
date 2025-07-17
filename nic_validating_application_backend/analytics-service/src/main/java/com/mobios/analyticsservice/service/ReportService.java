package com.mobios.analyticsservice.service;



import com.mobios.analyticsservice.client.FileServiceClient;
import com.mobios.analyticsservice.client.NICServiceClient;
import com.mobios.analyticsservice.dto.report.ReportRequest;
import com.mobios.analyticsservice.dto.report.ReportResponse;

import com.mobios.analyticsservice.dto.shared.NICRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final NICServiceClient nicServiceClient;
    private final FileServiceClient fileServiceClient;
    private final PDFReportGenerator pdfGenerator;
    private final CSVReportGenerator csvGenerator;
    private final ExcelReportGenerator excelGenerator;

    public ReportResponse generateReport(ReportRequest request) throws Exception {
        List<NICRecord> records = getRecordsForReport(request);
        records = filterRecords(records, request);

        ReportResponse response = new ReportResponse();
        response.setReportType(request.getReportType());
        response.setFormat(request.getFormat());
        response.setGeneratedBy("Analytics Service");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String baseFileName = "nic_report_" + timestamp;

        switch (request.getFormat().toLowerCase()) {
            case "pdf":
                String title = generateReportTitle(request);
                response.setContent(pdfGenerator.generateNICReport(records, title));
                response.setFileName(baseFileName + ".pdf");
                response.setContentType("application/pdf");
                break;

            case "csv":
                response.setContent(csvGenerator.generateNICReport(records));
                response.setFileName(baseFileName + ".csv");
                response.setContentType("text/csv");
                break;

            case "excel":
                title = generateReportTitle(request);
                response.setContent(excelGenerator.generateNICReport(records, title));
                response.setFileName(baseFileName + ".xlsx");
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;

            default:
                throw new IllegalArgumentException("Unsupported format: " + request.getFormat());
        }

        response.setFileSize((long) response.getContent().length);
        return response;
    }

    private List<NICRecord> getRecordsForReport(ReportRequest request) {
        switch (request.getReportType().toLowerCase()) {
            case "all":
                return nicServiceClient.getAllRecords();
            case "by_file":
                return nicServiceClient.getRecordsByFile(request.getFileName());
            case "by_user":
                return nicServiceClient.getRecordsByUser(request.getProcessedBy());
            case "by_files":
                return request.getFileNames().stream()
                        .flatMap(fileName -> nicServiceClient.getRecordsByFile(fileName).stream())
                        .collect(Collectors.toList());
            default:
                return nicServiceClient.getAllRecords();
        }
    }

    private List<NICRecord> filterRecords(List<NICRecord> records, ReportRequest request) {
        return records.stream()
                .filter(record -> {
                    if (request.getStartDate() != null && request.getEndDate() != null) {
                        if (record.getCreatedAt() != null) {
                            return !record.getCreatedAt().toLocalDate().isBefore(request.getStartDate()) &&
                                    !record.getCreatedAt().toLocalDate().isAfter(request.getEndDate());
                        }
                        return false;
                    }

                    if (!request.getIncludeInvalid() && record.getIsValid() != null && !record.getIsValid()) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private String generateReportTitle(ReportRequest request) {
        StringBuilder title = new StringBuilder("NIC Validation Report");

        switch (request.getReportType().toLowerCase()) {
            case "by_file":
                title.append(" - File: ").append(request.getFileName());
                break;
            case "by_user":
                title.append(" - User: ").append(request.getProcessedBy());
                break;
            case "by_files":
                title.append(" - Multiple Files");
                break;
            case "by_date_range":
                title.append(" - Date Range: ").append(request.getStartDate()).append(" to ").append(request.getEndDate());
                break;
        }

        return title.toString();
    }
}
