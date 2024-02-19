package com.epam.training.microservices.audio.storage.dto;

import com.epam.training.microservices.audio.storage.common.model.StorageType;
import lombok.Data;

@Data
public class StorageDto {
    private Long id;
    private StorageType type;
    private String bucket;
    private String path;
}
