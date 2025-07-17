package com.mobios.fileservice.service;

import com.mobios.fileservice.entity.FileMetadata;
import com.mobios.fileservice.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileProcessingService {


    private final FileMetadataRepository fileMetadataRepository;

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    public Map<String, Object> uploadFiles(MultipartFile[] files, String uploadedBy) {
        // Validate exactly 4 files
        if (files.length != 4) {
            throw new RuntimeException("Exactly 4 CSV files must be uploaded");
        }

        List<FileMetadata> uploadedFiles = new ArrayList<>();

        try {
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            for (MultipartFile file : files) {
                // Validate file is not empty
                if (file.isEmpty()) {
                    throw new RuntimeException("File cannot be empty: " + file.getOriginalFilename());
                }

                // Validate CSV format
                if (!isCSVFile(file)) {
                    throw new RuntimeException("Only CSV files are allowed: " + file.getOriginalFilename());
                }

//                 Validate file structure (basic CSV validation)
                if (!isValidCSVStructure(file)) {
                    throw new RuntimeException("Invalid CSV structure in file: " + file.getOriginalFilename());
                }

                // Generate unique filename
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String uniqueFileName = timestamp + "_" + file.getOriginalFilename();
                Path filePath = uploadDir.resolve(uniqueFileName);

                // Save file
                Files.copy(file.getInputStream(), filePath);

                // Create metadata with initial values
                FileMetadata metadata = new FileMetadata(
                        uniqueFileName,
                        file.getOriginalFilename(),
                        file.getSize(),
                        filePath.toString(),
                        uploadedBy
                );

                uploadedFiles.add(fileMetadataRepository.save(metadata));
            }

            return Map.of(
                    "message", "Files uploaded successfully",
                    "files", uploadedFiles
            );

        } catch (Exception e) {
            throw new RuntimeException("Error uploading files: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> parseCSVFiles(List<Long> fileIds) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Long fileId : fileIds) {
            Optional<FileMetadata> metadataOpt = fileMetadataRepository.findById(fileId);
            if (!metadataOpt.isPresent()) {
                continue;
            }

            FileMetadata metadata = metadataOpt.get();
            metadata.setProcessedStatus("PROCESSING");
            fileMetadataRepository.save(metadata);

            try {
                List<String> nicNumbers = parseCSVFile(metadata.getFilePath());

                // Update metadata with basic record count
                metadata.setTotalRecords(nicNumbers.size());
                metadata.setProcessedStatus("COMPLETED");
                fileMetadataRepository.save(metadata);

                results.add(Map.of(
                        "fileId", fileId,
                        "fileName", metadata.getOriginalName(),
                        "nicNumbers", nicNumbers,
                        "totalRecords", nicNumbers.size()
                ));

            } catch (Exception e) {
                metadata.setProcessedStatus("ERROR");
                fileMetadataRepository.save(metadata);
                throw new RuntimeException("Error processing file " + metadata.getOriginalName() + ": " + e.getMessage());
            }
        }

        return results;
    }

    private boolean isCSVFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        // Check file extension
        if (!originalFilename.toLowerCase().endsWith(".csv")) {
            return false;
        }

        // Check content type
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("text/csv") ||
                        contentType.equals("application/csv") ||
                        contentType.equals("text/plain")
        );
    }

    private boolean isValidCSVStructure(MultipartFile file) {
        try {
            // Save temp file for validation
            Path tempFile = Files.createTempFile("csv_validation", ".csv");
            Files.copy(file.getInputStream(), tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            try (BufferedReader tempReader = Files.newBufferedReader(tempFile)) {
                String line;
                boolean hasHeader = false;
                int lineCount = 0;

                while ((line = tempReader.readLine()) != null && lineCount < 10) { // Check first 10 lines
                    lineCount++;

                    if (lineCount == 1) {
                        hasHeader = true;
                        continue; // Skip header validation
                    }

                    // Basic validation: check if line has at least one field
                    String[] fields = line.split(",");
                    if (fields.length == 0 || (fields.length == 1 && fields[0].trim().isEmpty())) {
                        continue; // Skip empty lines
                    }

                    // Check if first field (NIC) is not empty
                    if (fields[0].trim().isEmpty()) {
                        Files.deleteIfExists(tempFile);
                        return false;
                    }
                }

                Files.deleteIfExists(tempFile);
                return hasHeader && lineCount > 1; // Must have header and at least one data row
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return false;
        }
    }


    private List<String> parseCSVFile(String filePath) throws IOException {
        List<String> nicNumbers = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] values = line.split(",");
                if (values.length > 0) {
                    String nic = values[0].trim();
                    if (!nic.isEmpty()) {
                        nicNumbers.add(nic);
                    }
                }
            }
        }

        return nicNumbers;
    }

    public List<FileMetadata> getFileMetadata(String uploadedBy) {
        return fileMetadataRepository.findByUploadedBy(uploadedBy);
    }

    public FileMetadata getFileMetadataById(Long id) {
        return fileMetadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public void deleteFile(Long id) {
        Optional<FileMetadata> metadataOpt = fileMetadataRepository.findById(id);
        if (metadataOpt.isPresent()) {
            FileMetadata metadata = metadataOpt.get();
            try {
                Files.deleteIfExists(Paths.get(metadata.getFilePath()));
                fileMetadataRepository.deleteById(id);
            } catch (IOException e) {
                throw new RuntimeException("Error deleting file: " + e.getMessage());
            }
        }
    }

    // Method to update file metadata after NIC validation (called from external service)
    public void updateValidationResults(Long fileId, int validRecords, int invalidRecords) {
        Optional<FileMetadata> metadataOpt = fileMetadataRepository.findById(fileId);
        if (metadataOpt.isPresent()) {
            FileMetadata metadata = metadataOpt.get();
            metadata.setValidRecords(validRecords);
            metadata.setInvalidRecords(invalidRecords);
            fileMetadataRepository.save(metadata);
        }
    }

    // Method to get file summary for external services
    public Map<String, Object> getFileSummary(Long fileId) {
        FileMetadata metadata = getFileMetadataById(fileId);
        return Map.of(
                "fileId", metadata.getId(),
                "fileName", metadata.getOriginalName(),
                "totalRecords", metadata.getTotalRecords(),
                "validRecords", metadata.getValidRecords(),
                "invalidRecords", metadata.getInvalidRecords(),
                "uploadedBy", metadata.getUploadedBy(),
                "uploadTime", metadata.getUploadTime(),
                "processedStatus", metadata.getProcessedStatus()
        );
    }

    //

    public Map<String, Object> getAllFileStatistics() {
        List<FileMetadata> allFiles = fileMetadataRepository.findAll();

        int totalFiles = allFiles.size();
        int totalRecords = allFiles.stream().mapToInt(FileMetadata::getTotalRecords).sum();
        int totalValidRecords = allFiles.stream().mapToInt(FileMetadata::getValidRecords).sum();
        int totalInvalidRecords = allFiles.stream().mapToInt(FileMetadata::getInvalidRecords).sum();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalFiles", totalFiles);
        statistics.put("totalRecords", totalRecords);
        statistics.put("totalValidRecords", totalValidRecords);
        statistics.put("totalInvalidRecords", totalInvalidRecords);

        return statistics;
    }

    public List<Map<String, Object>> getUserActivityStatistics() {
        List<Map<String, Object>> userActivity = new ArrayList<>();

        // Group files by user and count
        Map<String, Long> userFileCount = fileMetadataRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        FileMetadata::getUploadedBy,
                        Collectors.counting()
                ));

        userFileCount.forEach((username, fileCount) -> {
            Map<String, Object> activity = new HashMap<>();
            activity.put("username", username);
            activity.put("fileCount", fileCount);
            userActivity.add(activity);
        });

        return userActivity;
    }

    public List<Map<String, Object>> getProcessingTrends(Integer days) {
        List<Map<String, Object>> processingTrends = new ArrayList<>();

        int daysToShow = days != null ? days : 30;
        LocalDateTime startDate = LocalDateTime.now().minusDays(daysToShow);

        // Filter files uploaded within the specified range
        List<FileMetadata> recentFiles = fileMetadataRepository.findAll()
                .stream()
                .filter(file -> file.getUploadTime() != null && file.getUploadTime().isAfter(startDate))
                .collect(Collectors.toList());

        // Group files by upload date (yyyy-MM-dd)
        Map<String, List<FileMetadata>> dailyFiles = recentFiles.stream()
                .collect(Collectors.groupingBy(
                        file -> file.getUploadTime().toLocalDate().toString()
                ));

        // Build trends for each day
        dailyFiles.forEach((date, files) -> {
            Map<String, Object> trend = new HashMap<>();
            trend.put("date", date);
            trend.put("recordCount", files.stream().mapToInt(FileMetadata::getTotalRecords).sum());
            trend.put("fileCount", files.size());

            // Compute average processing time in seconds
            double avgProcessingTime = files.stream()
                    .filter(file -> file.getProcessedTime() != null && file.getUploadTime() != null)
                    .mapToLong(file ->
                            java.time.Duration.between(file.getUploadTime(), file.getProcessedTime()).getSeconds()
                    )
                    .average()
                    .orElse(0.0);

            trend.put("avgProcessingTime", avgProcessingTime);

            processingTrends.add(trend);
        });

        // Sort trends chronologically by date
        processingTrends.sort(Comparator.comparing(trend -> (String) trend.get("date")));

        return processingTrends;
    }

}
