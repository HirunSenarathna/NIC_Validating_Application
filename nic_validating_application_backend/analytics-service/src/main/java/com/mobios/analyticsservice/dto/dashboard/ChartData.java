package com.mobios.analyticsservice.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartData {

    private String label;
    private Long value;
    private String color;
    private Double percentage;
    private Object additionalData;
}
