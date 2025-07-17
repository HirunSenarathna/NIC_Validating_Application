package com.mobios.analyticsservice.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    private Long id;
    private String fileName;
    private String originalFileName;
    private Integer totalRecords;
    private Integer validRecords;
    private Integer invalidRecords;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String status;
}
