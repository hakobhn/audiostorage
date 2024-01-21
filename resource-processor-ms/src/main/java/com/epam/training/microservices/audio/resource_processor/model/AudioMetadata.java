package com.epam.training.microservices.audio.resource_processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioMetadata {
    private Long resourceId;
    private String name;
    private String artist;
    private String album;
    private String length;
    private String year;
}
