package com.mobios.nicservice.service;

import com.mobios.nicservice.entity.NICRecord;
import com.mobios.nicservice.repository.NICRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NICValidationService {


    private final NICRecordRepository nicRecordRepository;

    public Map<String, Object> validateNICNumbers(List<Map<String, Object>> fileData, String processedBy) {
        List<NICRecord> validatedRecords = new ArrayList<>();
        int totalValidated = 0;
        int totalValid = 0;
        int totalInvalid = 0;

        for (Map<String, Object> fileInfo : fileData) {
            String fileName = (String) fileInfo.get("fileName");
            List<String> nicNumbers = (List<String>) fileInfo.get("nicNumbers");

            for (String nicNumber : nicNumbers) {
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

        // Save all records
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
            // Check if it's old format (9 digits + V) or new format (12 digits)
            if (nicNumber.length() == 10 && nicNumber.endsWith("V")) {
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
        // Extract year, day of year, and gender
        String yearStr = nic.substring(0, 2);
        String dayStr = nic.substring(2, 5);

        int year = Integer.parseInt(yearStr);
        int dayOfYear = Integer.parseInt(dayStr);

        // Determine century (assume birth year is between 1900-2099)
        if (year >= 0 && year <= 99) {
            year = (year <= 25) ? 2000 + year : 1900 + year;
        }

        // Determine gender
        String gender;
        if (dayOfYear > 500) {
            gender = "Female";
            dayOfYear -= 500;
        } else {
            gender = "Male";
        }

        // Validate day of year
        if (dayOfYear < 1 || dayOfYear > 366) {
            return new NICValidationResult(false, null, null, null);
        }

        // Calculate birthday
        LocalDate birthday = LocalDate.ofYearDay(year, dayOfYear);

        // Calculate age
        int age = Period.between(birthday, LocalDate.now()).getYears();

        return new NICValidationResult(true, birthday, age, gender);
    }

    private NICValidationResult validateNewFormatNIC(String nic) {
        // Extract year, day of year, and gender
        String yearStr = nic.substring(0, 4);
        String dayStr = nic.substring(4, 7);

        int year = Integer.parseInt(yearStr);
        int dayOfYear = Integer.parseInt(dayStr);

        // Determine gender
        String gender;
        if (dayOfYear > 500) {
            gender = "Female";
            dayOfYear -= 500;
        } else {
            gender = "Male";
        }

        // Validate day of year
        if (dayOfYear < 1 || dayOfYear > 366) {
            return new NICValidationResult(false, null, null, null);
        }

        // Calculate birthday
        LocalDate birthday = LocalDate.ofYearDay(year, dayOfYear);

        // Calculate age
        int age = Period.between(birthday, LocalDate.now()).getYears();

        return new NICValidationResult(true, birthday, age, gender);
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
