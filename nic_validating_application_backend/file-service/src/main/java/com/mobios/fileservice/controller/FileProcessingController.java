package com.mobios.fileservice.controller;

import com.mobios.fileservice.entity.FileMetadata;
import com.mobios.fileservice.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileProcessingController {


    private final FileProcessingService fileProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("uploadedBy") String uploadedBy) {
        try {
            Map<String, Object> result = fileProcessingService.uploadFiles(files, uploadedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/parse")
    public ResponseEntity<?> parseFiles(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> fileIds = request.get("fileIds");
            List<Map<String, Object>> results = fileProcessingService.parseCSVFiles(fileIds);
            return ResponseEntity.ok(Map.of("results", results));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/metadata")
    public ResponseEntity<?> getFileMetadata(@RequestParam String uploadedBy) {
        try {
            List<FileMetadata> metadata = fileProcessingService.getFileMetadata(uploadedBy);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFileMetadataById(@PathVariable Long id) {
        try {
            FileMetadata metadata = fileProcessingService.getFileMetadataById(id);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getFileSummary(@PathVariable Long id) {
        try {
            Map<String, Object> summary = fileProcessingService.getFileSummary(id);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        try {
            fileProcessingService.deleteFile(id);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint for external  NIC service to update validation results
    @PutMapping("/{id}/validation-results")
    public ResponseEntity<?> updateValidationResults(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> validationResults) {
        try {
            Integer validRecords = validationResults.get("validRecords");
            Integer invalidRecords = validationResults.get("invalidRecords");

            fileProcessingService.updateValidationResults(id, validRecords, invalidRecords);
            return ResponseEntity.ok(Map.of("message", "Validation results updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint to get file validation statistics
    @GetMapping("/statistics")
    public ResponseEntity<?> getFileStatistics(@RequestParam String uploadedBy) {
        try {
            List<FileMetadata> files = fileProcessingService.getFileMetadata(uploadedBy);

            int totalFiles = files.size();
            int totalRecords = files.stream().mapToInt(f -> f.getTotalRecords()).sum();
            int totalValidRecords = files.stream().mapToInt(f -> f.getValidRecords()).sum();
            int totalInvalidRecords = files.stream().mapToInt(f -> f.getInvalidRecords()).sum();

            Map<String, Object> statistics = Map.of(
                    "totalFiles", totalFiles,
                    "totalRecords", totalRecords,
                    "totalValidRecords", totalValidRecords,
                    "totalInvalidRecords", totalInvalidRecords,
                    "files", files
            );

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/statistics/all")
    public ResponseEntity<?> getAllFileStatistics() {
        try {
            Map<String, Object> statistics = fileProcessingService.getAllFileStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

//    @GetMapping("/statistics/user-activity")
//    public ResponseEntity<?> getUserActivityStatistics() {
//        try {
//            List<Map<String, Object>> userActivity = fileProcessingService.getUserActivityStatistics();
//            return ResponseEntity.ok(userActivity);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }

    @GetMapping("/statistics/processing-trends")
    public ResponseEntity<?> getProcessingTrends(@RequestParam(required = false) Integer days) {
        try {
            List<Map<String, Object>> processingTrends = fileProcessingService.getProcessingTrends(days);
            return ResponseEntity.ok(processingTrends);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
