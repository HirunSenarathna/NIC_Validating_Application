package com.mobios.nicservice.service;

import com.mobios.nicservice.entity.NICRecord;
import com.mobios.nicservice.repository.NICRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NICValidationService {


    private final NICRecordRepository nicRecordRepository;

    // Month days - Sri Lanka NIC system uses 29 days for February regardless of leap year
    private static final int[] MONTH_DAYS = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public Map<String, Object> validateNICNumbers(List<Map<String, Object>> fileData, String processedBy) {
        List<NICRecord> validatedRecords = new ArrayList<>();
        int totalValidated = 0;
        int totalValid = 0;
        int totalInvalid = 0;

        for (Map<String, Object> fileInfo : fileData) {
            String fileName = (String) fileInfo.get("fileName");
            List<String> nicNumbers = (List<String>) fileInfo.get("nicNumbers");

            Set<String> seenNICsInFile = new HashSet<>();

            for (String nicNumber : nicNumbers) {
                if (seenNICsInFile.contains(nicNumber)) {
                    System.out.println("Duplicate NIC in same file skipped: " + nicNumber);
                    continue; // Skip duplicates within the same file
                }

                seenNICsInFile.add(nicNumber);

                NICValidationResult result = validateNIC(nicNumber);

                NICRecord record = new NICRecord(
                        nicNumber,
                        result.getBirthday(),
                        result.getAge(),
                        result.getGender(),
                        result.isValid(),
                        fileName,
                        processedBy
                );

                validatedRecords.add(record);
                totalValidated++;

                if (result.isValid()) {
                    totalValid++;
                } else {
                    totalInvalid++;
                }
            }
        }

        nicRecordRepository.saveAll(validatedRecords);

        return Map.of(
                "message", "NIC validation completed",
                "totalValidated", totalValidated,
                "totalValid", totalValid,
                "totalInvalid", totalInvalid,
                "records", validatedRecords
        );
    }

    private NICValidationResult validateNIC(String nicNumber) {
        if (nicNumber == null || nicNumber.trim().isEmpty()) {
            return new NICValidationResult(false, null, null, null);
        }

        nicNumber = nicNumber.trim().toUpperCase();

        try {
            // Check if it's old format (9 digits + V/X) or new format (12 digits)
            if (nicNumber.length() == 10 && (nicNumber.endsWith("V") || nicNumber.endsWith("X"))) {
                return validateOldFormatNIC(nicNumber);
            } else if (nicNumber.length() == 12 && nicNumber.matches("\\d{12}")) {
                return validateNewFormatNIC(nicNumber);
            } else {
                return new NICValidationResult(false, null, null, null);
            }
        } catch (Exception e) {
            return new NICValidationResult(false, null, null, null);
        }
    }

    private NICValidationResult validateOldFormatNIC(String nic) {
        // Extract year, day of year
        String yearStr = nic.substring(0, 2);
        String dayStr = nic.substring(2, 5);

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            System.out.println("NIC invalid: Invalid year format in NIC " + nic);
            return new NICValidationResult(false, null, null, null);
        }

        int dayOfYear;
        try {
            dayOfYear = Integer.parseInt(dayStr);
        } catch (NumberFormatException e) {
            System.out.println("NIC invalid: Invalid day format in NIC " + nic);
            return new NICValidationResult(false, null, null, null);
        }

        // Determine century (assume birth year is between 1900-2099)
        if (year >= 0 && year <= 99) {
            year = (year <= 25) ? 2000 + year : 1900 + year;
        } else {
            System.out.println("NIC invalid: Year out of valid range in NIC " + nic);
            return new NICValidationResult(false, null, null, null);
        }

        // Determine gender
        String gender;
        if (dayOfYear > 500) {
            gender = "Female";
            dayOfYear -= 500;
        } else {
            gender = "Male";
        }


        int actualDayOfYear = dayOfYear ;

        // Validate day of year (should be 0-365 for 0-based system)
        if (dayOfYear < 0 || dayOfYear > 365) {
            System.out.println("NIC invalid: Day of year " + dayOfYear + " out of range in NIC " + nic);
            return new NICValidationResult(false, null, null, null);
        }

        try {
            // Calculate birthday using Sri Lankan NIC month system
            LocalDate birthday = calculateBirthdayFromDayOfYear(year, actualDayOfYear);

            // Calculate age
            int age = Period.between(birthday, LocalDate.now()).getYears();

            return new NICValidationResult(true, birthday, age, gender);
        } catch (Exception e) {
            System.out.println("NIC invalid: Invalid date for NIC " + nic + " - " + e.getMessage());
            return new NICValidationResult(false, null, null, null);
        }
    }

    private NICValidationResult validateNewFormatNIC(String nic) {
        // Extract year, day of year
        String yearStr = nic.substring(0, 4);
        String dayStr = nic.substring(4, 7);

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            System.out.println("NIC invalid: Invalid year format in NIC " + nic);
            return new NICValidationResult(false, null, null, null);
        }

        int dayOfYear;
        try {
            dayOfYear = Integer.parseInt(dayStr);
        } catch (NumberFormatException e) {
            System.out.println("NIC invalid: Invalid day format in NIC " + nic);
            return new NICValidationResult(false, null, null, null);
        }

        // Determine gender
        String gender;
        if (dayOfYear > 500) {
            gender = "Female";
            dayOfYear -= 500;
        } else {
            gender = "Male";
        }


        int actualDayOfYear = dayOfYear ;

        // Validate day of year (should be 0-365 for 0-based system)
        if (dayOfYear < 0 || dayOfYear > 365) {
            System.out.println("NIC invalid: Day of year " + dayOfYear + " out of range in NIC " + nic);
            return new NICValidationResult(false, null, null, null);
        }

        try {
            // Calculate birthday using Sri Lankan NIC month system
            LocalDate birthday = calculateBirthdayFromDayOfYear(year, actualDayOfYear);

            // Calculate age
            int age = Period.between(birthday, LocalDate.now()).getYears();

            return new NICValidationResult(true, birthday, age, gender);
        } catch (Exception e) {
            System.out.println("NIC invalid: Invalid date for NIC " + nic + " - " + e.getMessage());
            return new NICValidationResult(false, null, null, null);
        }
    }


    private LocalDate calculateBirthdayFromDayOfYear(int year, int dayOfYear) {
        int month = 0;
        int day = dayOfYear;

        for (int i = 0; i < MONTH_DAYS.length; i++) {
            if (day <= MONTH_DAYS[i]) {
                month = i;
                break;
            }
            day -= MONTH_DAYS[i];
        }

        // Handle edge case where calculated day exceeds actual month days

        if (month == 1 && day == 29) { // February 29th
            // For non-leap years, adjust to February 28th
            if (!LocalDate.of(year, 1, 1).isLeapYear()) {
                day = 28;
            }
        }

        return LocalDate.of(year, month + 1, day); // month + 1 because LocalDate uses 1-based months
    }

    public List<NICRecord> getAllRecords() {
        return nicRecordRepository.findAll();
    }

    public List<NICRecord> getRecordsByFileName(String fileName) {
        return nicRecordRepository.findByFileName(fileName);
    }

    public List<NICRecord> getRecordsByUser(String processedBy) {
        return nicRecordRepository.findByProcessedBy(processedBy);
    }

    public Map<String, Object> getValidationStatistics() {
        return Map.of(
                "totalRecords", nicRecordRepository.count(),
                "validRecords", nicRecordRepository.countValidRecords(),
                "invalidRecords", nicRecordRepository.countInvalidRecords(),
                "maleRecords", nicRecordRepository.countMaleRecords(),
                "femaleRecords", nicRecordRepository.countFemaleRecords()
        );
    }

    public List<Map<String, Object>> getFileStatistics() {
        List<Object[]> results = nicRecordRepository.countRecordsByFileName();
        List<Map<String, Object>> statistics = new ArrayList<>();

        for (Object[] result : results) {
            statistics.add(Map.of(
                    "fileName", result[0],
                    "recordCount", result[1]
            ));
        }

        return statistics;
    }

    public Map<String, Long> getGenderStatistics() {
        Map<String, Long> genderStats = new HashMap<>();

        // Assuming you have a way to determine gender from NIC
        List<NICRecord> records = getAllRecords();

        long maleCount = records.stream()
                .filter(record -> record.getGender() != null && record.getGender().equalsIgnoreCase("MALE"))
                .count();

        long femaleCount = records.stream()
                .filter(record -> record.getGender() != null && record.getGender().equalsIgnoreCase("FEMALE"))
                .count();

        long unknownCount = records.stream()
                .filter(record -> record.getGender() == null ||
                        (!record.getGender().equalsIgnoreCase("MALE") && !record.getGender().equalsIgnoreCase("FEMALE")))
                .count();

        genderStats.put("MALE", maleCount);
        genderStats.put("FEMALE", femaleCount);
        genderStats.put("UNKNOWN", unknownCount);

        return genderStats;
    }

    public List<Map<String, Object>> getAgeDistribution() {
        List<Map<String, Object>> ageDistribution = new ArrayList<>();
        List<NICRecord> records = getAllRecords();

        // Group records by age ranges
        Map<String, Long> ageGroups = records.stream()
                .filter(record -> record.getAge() != null)
                .collect(Collectors.groupingBy(
                        record -> {
                            int age = record.getAge();
                            if (age < 18) return "Under 18";
                            else if (age < 25) return "18-24";
                            else if (age < 35) return "25-34";
                            else if (age < 45) return "35-44";
                            else if (age < 55) return "45-54";
                            else if (age < 65) return "55-64";
                            else return "65+";
                        },
                        Collectors.counting()
                ));

        ageGroups.forEach((ageGroup, count) -> {
            Map<String, Object> ageData = new HashMap<>();
            ageData.put("ageGroup", ageGroup);
            ageData.put("count", count);
            ageDistribution.add(ageData);
        });

        return ageDistribution;
    }

    public List<Map<String, Object>> getMonthlyTrends(Integer months) {
        List<Map<String, Object>> monthlyTrends = new ArrayList<>();
        List<NICRecord> records = getAllRecords();

        int monthsToShow = months != null ? months : 12;
        LocalDateTime startDate = LocalDateTime.now().minusMonths(monthsToShow);

        // Group records by month
        Map<String, Long> monthlyData = records.stream()
                .filter(record -> record.getValidationTime() != null && record.getValidationTime().isAfter(startDate))
                .collect(Collectors.groupingBy(
                        record -> record.getValidationTime().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ));

        monthlyData.forEach((month, count) -> {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("recordCount", count);
            monthlyTrends.add(monthData);
        });

        // Sort by month
        monthlyTrends.sort((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")));

        return monthlyTrends;
    }

    // Inner class for validation result
    private static class NICValidationResult {
        private boolean valid;
        private LocalDate birthday;
        private Integer age;
        private String gender;

        public NICValidationResult(boolean valid, LocalDate birthday, Integer age, String gender) {
            this.valid = valid;
            this.birthday = birthday;
            this.age = age;
            this.gender = gender;
        }

        public boolean isValid() { return valid; }
        public LocalDate getBirthday() { return birthday; }
        public Integer getAge() { return age; }
        public String getGender() { return gender; }
    }
}
