package com.mobios.fileservice.repository;

import com.mobios.fileservice.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByUploadedBy(String uploadedBy);
    List<FileMetadata> findByProcessedStatus(String status);
}
