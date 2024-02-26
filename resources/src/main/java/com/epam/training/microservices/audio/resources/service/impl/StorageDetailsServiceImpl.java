package com.epam.training.microservices.audio.resources.service.impl;

import com.epam.training.microservices.audio.resources.component.Tracer;
import com.epam.training.microservices.audio.resources.dto.StorageDetailsInput;
import com.epam.training.microservices.audio.resources.dto.StorageDetailsShort;
import com.epam.training.microservices.audio.resources.dto.StorageType;
import com.epam.training.microservices.audio.resources.service.StorageDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.epam.training.microservices.audio.resources.component.TracingConstants.CURRENT_TRACE_ID_HEADER;

@Slf4j
@Service
public class StorageDetailsServiceImpl implements StorageDetailsService {

    private static final String STORAGES_URL = "/storages";
    private final Tracer tracer;
    private final DiscoveryClient discoveryClient;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final URI serviceUri;

    private List<String> stagings = new ArrayList<>();

    public StorageDetailsServiceImpl(@Value("${storages.ms.base.uri}")
                                     String baseUri,
                                     @Value("${storages.service.id}")
                                     String serviceId,
                                     Tracer tracer,
                                     DiscoveryClient discoveryClient,
                                     CloseableHttpClient httpClient,
                                     ObjectMapper objectMapper) {
        this.tracer = tracer;
        this.discoveryClient = discoveryClient;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;

        serviceUri = discoveryClient.getInstances(serviceId).stream()
                .findAny().map(ServiceInstance::getUri)
                .orElse(URI.create(baseUri));
    }

    @Override
    @CircuitBreaker(name = "add", fallbackMethod = "fallbackForAddStorage")
    public StorageDetailsShort saveStaging(String bucket, String key) {
        try {
            HttpPost post = new HttpPost(serviceUri + STORAGES_URL);
            post.addHeader("content-type", "application/json");
            post.addHeader(CURRENT_TRACE_ID_HEADER, tracer.traceId());

            post.setEntity(new StringEntity(objectMapper.writeValueAsString(
                    StorageDetailsInput.builder()
                            .type(StorageType.STAGING)
                            .bucket(bucket)
                            .path(key)
                            .build())));
            CloseableHttpResponse response = httpClient.execute(post);
            return objectMapper.readValue(response.getEntity().getContent(), StorageDetailsShort.class);
        } catch (IOException e) {
            log.warn("Unable to send audio input.", e);
            throw new RuntimeException(e);
        }
    }

    public StorageDetailsShort fallbackForAddStorage(String bucket, String key, Throwable t) {
        log.warn("Got exception on saveStaging", t);
        stagings.add(key);
        Long id = Long.valueOf(stagings.indexOf(key));
        return StorageDetailsShort.builder()
                .id(id)
                .build();
    }

    public void makePermanent(Long id) {
        String key = stagings.get(id.intValue());
        stagings.remove(key);
    }
}
