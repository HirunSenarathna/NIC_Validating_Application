package com.mobios.analyticsservice.service;

import com.mobios.analyticsservice.dto.shared.NICRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class CSVReportGenerator {
    private static final String CSV_HEADER = "NIC Number,Birthday,Age,Gender,Valid,File Name,Processed By,Created At";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateNICReport(List<NICRecord> records) {
        try {
            log.info("Generating CSV report for {} records", records.size());

            StringWriter stringWriter = new StringWriter();

            // Write header
            stringWriter.append(CSV_HEADER).append("\n");

            // Write data rows
            for (NICRecord record : records) {
                stringWriter.append(formatCSVRow(record)).append("\n");
            }

            String csvContent = stringWriter.toString();
            log.info("Generated CSV content with {} characters", csvContent.length());

            return csvContent.getBytes("UTF-8");

        } catch (Exception e) {
            log.error("Error generating CSV report", e);
            throw new RuntimeException("Failed to generate CSV report: " + e.getMessage(), e);
        }
    }

    private String formatCSVRow(NICRecord record) {
        StringBuilder row = new StringBuilder();

        // NIC Number
        row.append(escapeCSVField(record.getNicNumber()));
        row.append(",");

        // Birthday
        String birthday = record.getBirthday() != null ?
                record.getBirthday().format(DATE_FORMATTER) : "";
        row.append(escapeCSVField(birthday));
        row.append(",");

        // Age
        row.append(record.getAge() != null ? record.getAge() : "");
        row.append(",");

        // Gender
        row.append(escapeCSVField(record.getGender()));
        row.append(",");

        // Valid
        row.append(record.getIsValid() != null ? record.getIsValid() : "");
        row.append(",");

        // File Name
        row.append(escapeCSVField(record.getFileName()));
        row.append(",");

        // Processed By
        row.append(escapeCSVField(record.getProcessedBy()));
        row.append(",");

        // Created At
        String createdAt = record.getCreatedAt() != null ?
                record.getCreatedAt().format(DATETIME_FORMATTER) : "";
        row.append(escapeCSVField(createdAt));

        return row.toString();
    }

    private String escapeCSVField(String field) {
        if (field == null) {
            return "";
        }

        // If field contains comma, newline, or quotes, wrap in quotes and escape internal quotes
        if (field.contains(",") || field.contains("\n") || field.contains("\"")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        return field;
    }
}
