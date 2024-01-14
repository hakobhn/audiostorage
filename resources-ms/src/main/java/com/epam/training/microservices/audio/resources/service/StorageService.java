package com.epam.training.microservices.audio.resources.service;

import java.io.InputStream;

public interface StorageService {
    String store(String name, byte[] bytes);
    InputStream read(String location);
    void delete(String location);
}
