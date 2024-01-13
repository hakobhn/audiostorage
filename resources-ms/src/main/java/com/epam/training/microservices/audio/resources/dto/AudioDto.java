package com.epam.training.microservices.audio.resources.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AudioDto {
    private long id;
    private String name;
    private String location;
    private String size;
    private long bytes;
    @JsonFormat(pattern="dd-MM-yyyy")
    private LocalDate postedAt;
}
