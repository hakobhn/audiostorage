package com.epam.training.microservices.audio.resources.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@Builder
public class AudioInput {
    private String name;
    @NotNull(message = "Location is required")
    private String location;
    private long bytes;
}
