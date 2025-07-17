package com.mobios.analyticsservice.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {

    private Long totalRecords;
    private Long validRecords;
    private Long invalidRecords;
    private Long maleUsers;
    private Long femaleUsers;
    private Long totalFiles;
    private Long totalUsers;
    private Double validationSuccessRate;
    private List<ChartData> genderDistribution;
    private List<ChartData> validationStatusDistribution;
    private List<ChartData> fileProcessingStats;
    private List<ChartData> monthlyProcessingTrends;
    private List<ChartData> userActivityStats;
    private List<ChartData> ageDistribution;
    private Map<String, Object> additionalMetrics;
}
