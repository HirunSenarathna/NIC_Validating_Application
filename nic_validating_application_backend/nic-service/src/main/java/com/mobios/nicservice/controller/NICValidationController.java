package com.mobios.nicservice.controller;

import com.mobios.nicservice.entity.NICRecord;
import com.mobios.nicservice.service.NICValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nic")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NICValidationController {

    private final NICValidationService nicValidationService;

    @PostMapping("/validate")
    public ResponseEntity<?> validateNICNumbers(@RequestBody Map<String, Object> request) {
        try {
            List<Map<String, Object>> fileData = (List<Map<String, Object>>) request.get("fileData");
            String processedBy = (String) request.get("processedBy");

            Map<String, Object> result = nicValidationService.validateNICNumbers(fileData, processedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/records")
    public ResponseEntity<?> getAllRecords() {
        try {
            List<NICRecord> records = nicValidationService.getAllRecords();
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/records/file/{fileName}")
    public ResponseEntity<?> getRecordsByFile(@PathVariable String fileName) {
        try {
            List<NICRecord> records = nicValidationService.getRecordsByFileName(fileName);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/records/user/{processedBy}")
    public ResponseEntity<?> getRecordsByUser(@PathVariable String processedBy) {
        try {
            List<NICRecord> records = nicValidationService.getRecordsByUser(processedBy);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getValidationStatistics() {
        try {
            Map<String, Object> statistics = nicValidationService.getValidationStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/statistics/files")
    public ResponseEntity<?> getFileStatistics() {
        try {
            List<Map<String, Object>> statistics = nicValidationService.getFileStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
