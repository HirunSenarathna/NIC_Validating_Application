package com.mobios.fileservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "file_name", nullable = false)
//    private String fileName;
//
//    @Column(name = "original_name", nullable = false)
//    private String originalName;
//
//    @Column(name = "file_size")
//    private Long fileSize;
//
//    @Column(name = "file_path")
//    private String filePath;
//
//    @Column(name = "upload_time")
//    private LocalDateTime uploadTime;
//
//    @Column(name = "processed_status")
//    private String processedStatus; // PENDING, PROCESSING, COMPLETED, ERROR
//
//    @Column(name = "total_records")
//    private Integer totalRecords;
//
//    @Column(name = "valid_records")
//    private Integer validRecords;
//
//    @Column(name = "invalid_records")
//    private Integer invalidRecords;
//
//    @Column(name = "uploaded_by")
//    private String uploadedBy;
//
//    public FileMetadata(String uniqueFileName, String originalFilename, long size, String string, String uploadedBy) {
//        this.fileName = uniqueFileName;
//        this.originalName = originalFilename;
//        this.fileSize = size;
//        this.processedStatus = string;
//        this.uploadedBy = uploadedBy;
//    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    @Column(name = "processed_status")
    private String processedStatus = "PENDING";

    @Column(name = "total_records")
    private Integer totalRecords = 0;

    @Column(name = "valid_records")
    private Integer validRecords = 0;

    @Column(name = "invalid_records")
    private Integer invalidRecords = 0;

    @Column(name = "processed_time")
    private LocalDateTime processedTime;

    // Constructor for creating new file metadata
    public FileMetadata(String fileName, String originalName, Long fileSize,
                        String filePath, String uploadedBy) {
        this.fileName = fileName;
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.uploadedBy = uploadedBy;
        this.uploadTime = LocalDateTime.now();
        this.processedStatus = "PENDING";
        this.totalRecords = 0;
        this.validRecords = 0;
        this.invalidRecords = 0;
    }

    // Setter for processed status that also updates processed time
    public void setProcessedStatus(String processedStatus) {
        this.processedStatus = processedStatus;
        if ("COMPLETED".equals(processedStatus) || "ERROR".equals(processedStatus)) {
            this.processedTime = LocalDateTime.now();
        }
    }
}
