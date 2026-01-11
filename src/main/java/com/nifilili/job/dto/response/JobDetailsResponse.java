package com.nifilili.job.dto.response;

import com.nifilili.core.enums.job.JobType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class JobDetailsResponse {

    private String title;
    private String description;
    private JobType jobType;
    private Long municipalityId;
    private Long wardNumber;
    private String toleName;
    private String postalCode;
    private boolean remote;
    private Double salaryRangeMin;
    private Double salaryRangeMax;
    private Integer numberOfOpenings;
    private LocalDate applicationDeadline;
    private List<String> skills;
}
