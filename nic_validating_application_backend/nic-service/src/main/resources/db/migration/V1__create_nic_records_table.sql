CREATE TABLE nic_records (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             nic_number VARCHAR(20) NOT NULL,
                             birthday DATE,
                             age INT,
                             gender VARCHAR(10),
                             is_valid BOOLEAN,
                             file_name VARCHAR(255),
                             validation_time DATETIME,
                             processed_by VARCHAR(100),
                             CONSTRAINT unique_nic_number_file_name UNIQUE (nic_number, file_name)
);
