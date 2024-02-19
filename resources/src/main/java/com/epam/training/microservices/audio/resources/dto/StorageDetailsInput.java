package com.epam.training.microservices.audio.resources.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageDetailsInput {
    private StorageType type;
    private String bucket;
    private String path;
}
