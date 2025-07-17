package com.mobios.nicservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nic_records",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nic_number", "file_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class NICRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nic_number", nullable = false)
    private String nicNumber;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender;

    @Column(name = "is_valid")
    private Boolean isValid;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "validation_time")
    private LocalDateTime validationTime;

    @Column(name = "processed_by")
    private String processedBy;

    public NICRecord(String nicNumber, LocalDate birthday, Integer age, String gender, boolean valid, String fileName, String processedBy) {
        this.nicNumber = nicNumber;
        this.birthday = birthday;
        this.age = age;
        this.gender = gender;
        this.isValid = valid;
        this.fileName = fileName;
        this.processedBy = processedBy;
    }
}
