package com.epam.training.microservices.audio.resources.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.epam.training.microservices.audio.resources.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Primary
@Service("AwsStorageService")
public class AwsStorageService implements StorageService {

    @Value("${aws.storage.bucket}")
    private String bucketName;

    @Autowired
    private AmazonS3 amazonS3Client;

    @PostConstruct
    private void postConstruct() {
        if(amazonS3Client.doesBucketExistV2(bucketName)) {
            return;
        }
        amazonS3Client.createBucket(bucketName);
    }

    private static final DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String store(String name, byte[] bytes) {
        int contentLength = bytes.length;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        String stringObjKeyName = timeStampPattern.format(java.time.LocalDateTime.now()) + "_" + name;

        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            amazonS3Client.putObject(new PutObjectRequest(bucketName, stringObjKeyName, inputStream, metadata));
            return stringObjKeyName;
        } catch (Exception e) {
            log.warn("Unable to upload into AWS", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] read(String key) {
        S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucketName, key));
        try (InputStream inputStream = object.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        } catch (Exception e) {
            log.warn("Unable to read object", e);
            throw new RuntimeException(e);
        }
    }

    public void delete(String keyName) {
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, keyName));
        } catch (Exception e) {
            log.warn("Failed to delete {} file", keyName, e);
        }
    }
}
