package com.epam.training.microservices.audio.resources.service;

import com.epam.training.microservices.audio.resources.dto.StorageDetailsShort;

public interface StorageDetailsService {

    StorageDetailsShort saveStaging(String bucket, String key);
    void makePermanent(Long id);

}
