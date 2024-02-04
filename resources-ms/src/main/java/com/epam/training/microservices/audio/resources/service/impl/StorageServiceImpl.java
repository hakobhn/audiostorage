package com.epam.training.microservices.audio.resources.service.impl;

import com.epam.training.microservices.audio.resources.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service("FsStorageService")
public class StorageServiceImpl implements StorageService {

    private static final DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${storage.home}")
    private String storageHome;

    @Override
    public String store(String name, byte[] bytes) {
        File file = new File(storageHome + timeStampPattern.format(java.time.LocalDateTime.now()) + "_" + name);
        try (FileOutputStream outStream = new FileOutputStream(file);
             InputStream inputStream = new ByteArrayInputStream(bytes)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            log.warn("Unable to store file {}", name, e);
            throw new RuntimeException(e);
        }
        return file.getPath();
    }

    @Override
    public byte[] read(String location) {
        File file = new File(location);
        try (InputStream inputStream = new FileInputStream(file)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.warn("Unable to read file {}", location, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String location) {
        try {
            log.info("Trying to delete file: {}", location);
            Files.delete(Path.of(location));
        } catch (Exception e) {
            log.warn("Failed to delete {} file", location, e);
            throw new RuntimeException(e);
        }
    }
}
