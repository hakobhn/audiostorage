package com.epam.training.microservices.audio.resources.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AudioInput {
    private String name;
    private String location;
    private long bytes;
}
