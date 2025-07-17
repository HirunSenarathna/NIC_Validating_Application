package com.mobios.analyticsservice.client;

import com.mobios.analyticsservice.dto.shared.FileMetadata;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "file-service", url = "${application.config.files-url}")
public interface FileServiceClient {

    @GetMapping("/metadata")
    List<FileMetadata> getFileMetadata(@RequestParam String uploadedBy);

    @GetMapping("/{id}")
    FileMetadata getFileMetadataById(@PathVariable Long id);

    @GetMapping("/statistics")
    Map<String, Object> getFileStatistics(@RequestParam String uploadedBy);

    @GetMapping("/statistics/all")
    Map<String, Object> getAllFileStatistics();

    @GetMapping("/statistics/user-activity")
    List<Map<String, Object>> getUserActivityStatistics();

    @GetMapping("/statistics/processing-trends")
    List<Map<String, Object>> getProcessingTrends(@RequestParam(required = false) Integer days);
}
