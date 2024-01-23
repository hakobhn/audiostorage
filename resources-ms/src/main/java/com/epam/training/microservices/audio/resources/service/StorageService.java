package com.epam.training.microservices.audio.resources.service;

public interface StorageService {
    String store(String name, byte[] bytes);
    byte[] read(String location);
    void delete(String location);
}
