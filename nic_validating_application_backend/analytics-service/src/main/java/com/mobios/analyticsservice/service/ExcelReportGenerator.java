package com.mobios.analyticsservice.service;


import com.mobios.analyticsservice.dto.shared.NICRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelReportGenerator {

    public byte[] generateNICReport(List<NICRecord> records, String title) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("NIC Report");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        // Create title row
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        // Create header row
        Row headerRow = sheet.createRow(2);
        String[] headers = {"NIC Number", "File Name", "Birthday", "Age", "Gender", "Valid", "Processed By", "Created At"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Add data rows
        int rowNum = 3;
        for (NICRecord record : records) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(record.getNicNumber());
            row.createCell(1).setCellValue(record.getFileName());
            row.createCell(2).setCellValue(record.getBirthday() != null ? record.getBirthday().toString() : "");
            row.createCell(3).setCellValue(record.getAge() != null ? record.getAge().toString() : "");
            row.createCell(4).setCellValue(record.getGender() != null ? record.getGender() : "");
            row.createCell(5).setCellValue(record.getIsValid() != null ? (record.getIsValid() ? "Yes" : "No") : "");
            row.createCell(6).setCellValue(record.getProcessedBy());
            row.createCell(7).setCellValue(record.getCreatedAt() != null ?
                    record.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");

            // Apply data style to all cells
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Add summary sheet
        Sheet summarySheet = workbook.createSheet("Summary");
        Row summaryTitleRow = summarySheet.createRow(0);
        summaryTitleRow.createCell(0).setCellValue("Report Summary");
        summaryTitleRow.getCell(0).setCellStyle(titleStyle);

        long validCount = records.stream().filter(r -> r.getIsValid() != null && r.getIsValid()).count();
        long invalidCount = records.size() - validCount;

        summarySheet.createRow(2).createCell(0).setCellValue("Total Records: " + records.size());
        summarySheet.createRow(3).createCell(0).setCellValue("Valid Records: " + validCount);
        summarySheet.createRow(4).createCell(0).setCellValue("Invalid Records: " + invalidCount);

        summarySheet.autoSizeColumn(0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }
}
