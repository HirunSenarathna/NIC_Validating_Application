package com.mobios.analyticsservice.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NICRecord {
    private Long id;
    private String nicNumber;
    private String fileName;
    private LocalDate birthday;
    private Integer age;
    private String gender;
    private Boolean isValid;
    private String processedBy;
    private LocalDateTime createdAt;
}
