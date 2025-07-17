package com.mobios.analyticsservice.service;

import com.mobios.analyticsservice.dto.shared.NICRecord;
import org.springframework.stereotype.Service;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PDFReportGenerator {
    public byte[] generateNICReport(List<NICRecord> records, String title) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph titleParagraph = new Paragraph(title, titleFont);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(20);
        document.add(titleParagraph);

        // Add generation date
        Font dateFont = new Font(Font.FontFamily.HELVETICA, 10);
        Paragraph dateParagraph = new Paragraph("Generated on: " +
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
        dateParagraph.setAlignment(Element.ALIGN_RIGHT);
        dateParagraph.setSpacingAfter(20);
        document.add(dateParagraph);

        // Create table
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Set column widths
        float[] columnWidths = {2f, 2f, 1.5f, 1f, 1f, 1.5f, 2f};
        table.setWidths(columnWidths);

        // Add headers
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        addCell(table, "NIC Number", headerFont, BaseColor.LIGHT_GRAY);
        addCell(table, "File Name", headerFont, BaseColor.LIGHT_GRAY);
        addCell(table, "Birthday", headerFont, BaseColor.LIGHT_GRAY);
        addCell(table, "Age", headerFont, BaseColor.LIGHT_GRAY);
        addCell(table, "Gender", headerFont, BaseColor.LIGHT_GRAY);
        addCell(table, "Valid", headerFont, BaseColor.LIGHT_GRAY);
        addCell(table, "Processed By", headerFont, BaseColor.LIGHT_GRAY);

        // Add data rows
        Font dataFont = new Font(Font.FontFamily.HELVETICA, 9);
        for (NICRecord record : records) {
            addCell(table, record.getNicNumber(), dataFont, BaseColor.WHITE);
            addCell(table, record.getFileName(), dataFont, BaseColor.WHITE);
            addCell(table, record.getBirthday() != null ? record.getBirthday().toString() : "N/A", dataFont, BaseColor.WHITE);
            addCell(table, record.getAge() != null ? record.getAge().toString() : "N/A", dataFont, BaseColor.WHITE);
            addCell(table, record.getGender() != null ? record.getGender() : "N/A", dataFont, BaseColor.WHITE);
            addCell(table, record.getIsValid() != null ? (record.getIsValid() ? "Yes" : "No") : "N/A", dataFont, BaseColor.WHITE);
            addCell(table, record.getProcessedBy(), dataFont, BaseColor.WHITE);
        }

        document.add(table);

        // Add summary
        Paragraph summary = new Paragraph("\nSummary:", headerFont);
        summary.setSpacingBefore(20);
        document.add(summary);

        long validCount = records.stream().filter(r -> r.getIsValid() != null && r.getIsValid()).count();
        long invalidCount = records.size() - validCount;

        document.add(new Paragraph("Total Records: " + records.size(), dataFont));
        document.add(new Paragraph("Valid Records: " + validCount, dataFont));
        document.add(new Paragraph("Invalid Records: " + invalidCount, dataFont));

        document.close();

        return baos.toByteArray();
    }

    private void addCell(PdfPTable table, String text, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
