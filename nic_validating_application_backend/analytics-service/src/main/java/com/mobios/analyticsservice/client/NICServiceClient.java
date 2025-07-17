package com.mobios.analyticsservice.client;

import com.mobios.analyticsservice.dto.shared.NICRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "nic-service", url = "${application.config.nic-url}")
public interface NICServiceClient {

    @GetMapping("/records")
    List<NICRecord> getAllRecords();

    @GetMapping("/records/file/{fileName}")
    List<NICRecord> getRecordsByFile(@PathVariable String fileName);

    @GetMapping("/records/user/{processedBy}")
    List<NICRecord> getRecordsByUser(@PathVariable String processedBy);

    @GetMapping("/statistics")
    Map<String, Object> getValidationStatistics();

    @GetMapping("/statistics/files")
    List<Map<String, Object>> getFileStatistics();

    @GetMapping("/statistics/gender")
    Map<String, Long> getGenderStatistics();

    @GetMapping("/statistics/age-distribution")
    List<Map<String, Object>> getAgeDistribution();

    @GetMapping("/statistics/monthly-trends")
    List<Map<String, Object>> getMonthlyTrends(@RequestParam(required = false) Integer months);
}
