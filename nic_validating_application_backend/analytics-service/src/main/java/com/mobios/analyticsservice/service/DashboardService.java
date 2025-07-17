package com.mobios.analyticsservice.service;

import com.mobios.analyticsservice.client.FileServiceClient;
import com.mobios.analyticsservice.client.NICServiceClient;
import com.mobios.analyticsservice.dto.dashboard.ChartData;
import com.mobios.analyticsservice.dto.dashboard.DashboardSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final NICServiceClient nicServiceClient;
    private final FileServiceClient fileServiceClient;
//    private final AuthServiceClient authServiceClient;

    public DashboardSummary getDashboardSummary() {
        try {
            DashboardSummary summary = new DashboardSummary();

            Map<String, Object> validationStats = nicServiceClient.getValidationStatistics();
            Map<String, Object> fileStats = fileServiceClient.getAllFileStatistics();
//            Map<String, Object> userStats = authServiceClient.getUserStatistics();

            summary.setTotalRecords(getLongValue(validationStats, "totalRecords"));
            summary.setValidRecords(getLongValue(validationStats, "validRecords"));
            summary.setInvalidRecords(getLongValue(validationStats, "invalidRecords"));
            summary.setTotalFiles(getLongValue(fileStats, "totalFiles"));
//            summary.setTotalUsers(getLongValue(userStats, "totalUsers"));

            if (summary.getTotalRecords() > 0) {
                summary.setValidationSuccessRate(
                        (double) summary.getValidRecords() / summary.getTotalRecords() * 100);
            }

            Map<String, Long> genderStats = nicServiceClient.getGenderStatistics();
            summary.setMaleUsers(genderStats.getOrDefault("MALE", 0L));
            summary.setFemaleUsers(genderStats.getOrDefault("FEMALE", 0L));

            summary.setGenderDistribution(generateGenderChartData(genderStats));
            summary.setValidationStatusDistribution(generateValidationStatusChartData(summary));
            summary.setFileProcessingStats(generateFileProcessingChartData());
            summary.setMonthlyProcessingTrends(generateMonthlyTrendsChartData());
            summary.setUserActivityStats(generateUserActivityChartData());
            summary.setAgeDistribution(generateAgeDistributionChartData());
            summary.setAdditionalMetrics(generateAdditionalMetrics());

            return summary;

        } catch (Exception e) {
            log.error("Error generating dashboard summary", e);
            throw new RuntimeException("Failed to generate dashboard summary: " + e.getMessage());
        }
    }

    private List<ChartData> generateGenderChartData(Map<String, Long> genderStats) {
        List<ChartData> chartData = new ArrayList<>();

        Long maleCount = genderStats.getOrDefault("MALE", 0L);
        Long femaleCount = genderStats.getOrDefault("FEMALE", 0L);
//        Long unknownCount = genderStats.getOrDefault("UNKNOWN", 0L);
        Long total = maleCount + femaleCount;

        if (total > 0) {
            chartData.add(new ChartData("Male", maleCount, "#3B82F6",
                    (double) maleCount / total * 100, null));
            chartData.add(new ChartData("Female", femaleCount, "#EF4444",
                    (double) femaleCount / total * 100, null));
//            if (unknownCount > 0) {
//                chartData.add(new ChartData("Unknown", unknownCount, "#6B7280",
//                        (double) unknownCount / total * 100, null));
//            }
        }

        return chartData;
    }

    private List<ChartData> generateValidationStatusChartData(DashboardSummary summary) {
        List<ChartData> chartData = new ArrayList<>();

        Long total = summary.getTotalRecords();
        if (total > 0) {
            chartData.add(new ChartData("Valid", summary.getValidRecords(), "#10B981",
                    (double) summary.getValidRecords() / total * 100, null));
            chartData.add(new ChartData("Invalid", summary.getInvalidRecords(), "#EF4444",
                    (double) summary.getInvalidRecords() / total * 100, null));
        }

        return chartData;
    }

    private List<ChartData> generateFileProcessingChartData() {
        try {
            List<Map<String, Object>> fileStats = nicServiceClient.getFileStatistics();
            return fileStats.stream()
                    .map(stat -> new ChartData(
                            (String) stat.get("fileName"),
                            getLongValue(stat, "recordCount"),
                            generateRandomColor(),
                            null,
                            stat
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error generating file processing chart data", e);
            return new ArrayList<>();
        }
    }

    private List<ChartData> generateMonthlyTrendsChartData() {
        try {
            List<Map<String, Object>> monthlyTrends = nicServiceClient.getMonthlyTrends(12);
            return monthlyTrends.stream()
                    .map(trend -> new ChartData(
                            (String) trend.get("month"),
                            getLongValue(trend, "recordCount"),
                            "#3B82F6",
                            null,
                            trend
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error generating monthly trends chart data", e);
            return new ArrayList<>();
        }
    }

    private List<ChartData> generateUserActivityChartData() {
        try {
            List<Map<String, Object>> userActivity = fileServiceClient.getUserActivityStatistics();
            return userActivity.stream()
                    .map(activity -> new ChartData(
                            (String) activity.get("username"),
                            getLongValue(activity, "fileCount"),
                            generateRandomColor(),
                            null,
                            activity
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error generating user activity chart data", e);
            return new ArrayList<>();
        }
    }

    private List<ChartData> generateAgeDistributionChartData() {
        try {
            List<Map<String, Object>> ageDistribution = nicServiceClient.getAgeDistribution();
            return ageDistribution.stream()
                    .map(age -> new ChartData(
                            (String) age.get("ageGroup"),
                            getLongValue(age, "count"),
                            generateRandomColor(),
                            null,
                            age
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error generating age distribution chart data", e);
            return new ArrayList<>();
        }
    }

    private Map<String, Object> generateAdditionalMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            List<Map<String, Object>> processingTrends = fileServiceClient.getProcessingTrends(30);
//            List<Map<String, Object>> activeUsers = authServiceClient.getActiveUsers(30);

            metrics.put("avgProcessingTime", calculateAverageProcessingTime(processingTrends));
            metrics.put("peakProcessingDay", findPeakProcessingDay(processingTrends));
//            metrics.put("activeUsersLast30Days", activeUsers.size());
//            metrics.put("mostActiveUser", findMostActiveUser(activeUsers));

        } catch (Exception e) {
            log.error("Error generating additional metrics", e);
        }

        return metrics;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    private String generateRandomColor() {
        String[] colors = {"#3B82F6", "#EF4444", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#6B7280"};
        return colors[new Random().nextInt(colors.length)];
    }

    private Double calculateAverageProcessingTime(List<Map<String, Object>> trends) {
        if (trends.isEmpty()) return 0.0;

        return trends.stream()
                .mapToDouble(trend -> getDoubleValue(trend, "avgProcessingTime"))
                .average()
                .orElse(0.0);
    }

    private String findPeakProcessingDay(List<Map<String, Object>> trends) {
        return trends.stream()
                .max(Comparator.comparing(trend -> getLongValue(trend, "recordCount")))
                .map(trend -> (String) trend.get("date"))
                .orElse("N/A");
    }

    private String findMostActiveUser(List<Map<String, Object>> activeUsers) {
        return activeUsers.stream()
                .max(Comparator.comparing(user -> getLongValue(user, "activityCount")))
                .map(user -> (String) user.get("username"))
                .orElse("N/A");
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
}
