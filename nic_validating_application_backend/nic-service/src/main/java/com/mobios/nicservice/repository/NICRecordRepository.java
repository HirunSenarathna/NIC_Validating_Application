package com.mobios.nicservice.repository;

import com.mobios.nicservice.entity.NICRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NICRecordRepository extends JpaRepository<NICRecord, Long> {

    List<NICRecord> findByFileName(String fileName);
    List<NICRecord> findByProcessedBy(String processedBy);
    List<NICRecord> findByIsValid(Boolean isValid);

    @Query("SELECT COUNT(n) FROM NICRecord n WHERE n.isValid = true")
    Long countValidRecords();

    @Query("SELECT COUNT(n) FROM NICRecord n WHERE n.isValid = false")
    Long countInvalidRecords();

    @Query("SELECT COUNT(n) FROM NICRecord n WHERE n.gender = 'Male'")
    Long countMaleRecords();

    @Query("SELECT COUNT(n) FROM NICRecord n WHERE n.gender = 'Female'")
    Long countFemaleRecords();

    @Query("SELECT n.fileName, COUNT(n) FROM NICRecord n GROUP BY n.fileName")
    List<Object[]> countRecordsByFileName();
}
