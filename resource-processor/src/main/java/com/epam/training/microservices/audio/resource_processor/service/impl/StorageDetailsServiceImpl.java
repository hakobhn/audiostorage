package com.epam.training.microservices.audio.resource_processor.service.impl;


import com.epam.training.microservices.audio.resource_processor.service.ResourcesService;
import com.epam.training.microservices.audio.resource_processor.service.StorageDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.net.URI;

@Slf4j
@Service
public class StorageDetailsServiceImpl implements StorageDetailsService {

    private static final String STORAGES_URL = "/storages";

    private final DiscoveryClient discoveryClient;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final URI serviceUri;
    private final ResourcesService resourcesService;

    public StorageDetailsServiceImpl(@Value("${storages.ms.base.uri}")
                                     String baseUri,
                                     @Value("${storages.service.id}")
                                     String serviceId,
                                     DiscoveryClient discoveryClient,
                                     CloseableHttpClient httpClient,
                                     ObjectMapper objectMapper,
                                     ResourcesService resourcesService) {
        this.discoveryClient = discoveryClient;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.resourcesService = resourcesService;

        serviceUri = discoveryClient.getInstances(serviceId).stream()
                .findAny().map(ServiceInstance::getUri)
                .orElse(URI.create(baseUri));
    }

    @Override
    @CircuitBreaker(name = "makePermanent", fallbackMethod = "fallbackForMakePermanent")
    public void makePermanent(Long id) {
        HttpPatch patch = new HttpPatch(serviceUri + STORAGES_URL + "/" + id);

        try (CloseableHttpResponse response = httpClient.execute(patch)) {
            log.info("Song delete request sent response {}", response.getStatusLine());
        } catch (Exception e) {
            log.warn("Unable to delete song.", e);
            throw new RuntimeException(e);
        }
    }

    public void fallbackForMakePermanent(Long id, Throwable t) {
        log.warn("Got exception on makePermanent", t);
        resourcesService.makePermanent(id);
    }
}
