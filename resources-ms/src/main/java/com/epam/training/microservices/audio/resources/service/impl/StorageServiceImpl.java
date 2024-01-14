package com.epam.training.microservices.audio.resources.service.impl;

import com.epam.training.microservices.audio.resources.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    private static final DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${storage.home}")
    private String storageHome;

    public String store(String name, byte[] bytes) {
        File file = new File(storageHome + timeStampPattern.format(java.time.LocalDateTime.now()) + "_" + name);
        try (FileOutputStream outStream = new FileOutputStream(file);
             InputStream inputStream = new ByteArrayInputStream(bytes)){
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return file.getPath();
    }

    @Override
    public InputStream read(String location) {
        File file = new File(location);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String location) {
        try {
            Files.delete(Paths.get(location));
        } catch (IOException e) {
            log.warn("Failed to delete {} file", location, e);
        }
    }
}
