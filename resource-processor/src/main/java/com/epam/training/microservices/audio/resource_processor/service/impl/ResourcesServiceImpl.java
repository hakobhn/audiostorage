package com.epam.training.microservices.audio.resource_processor.service.impl;

import com.epam.training.microservices.audio.resource_processor.model.AudioInput;
import com.epam.training.microservices.audio.resource_processor.model.AudioShort;
import com.epam.training.microservices.audio.resource_processor.service.ResourcesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Service
public class ResourcesServiceImpl implements ResourcesService {

    private static final String RESOURCES_URL = "/resources";
    private static final String RESOURCES_DELETE_BY_KEY_URL = RESOURCES_URL + "/deleteByKey";
    private static final String RESOURCES_MAKE_PERMANENT_URL = RESOURCES_URL + "/makePermanent";

    private final String serviceId;

    private final DiscoveryClient discoveryClient;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final URI serviceUri;

    public ResourcesServiceImpl(@Value("${resources.ms.base.uri}")
                                String baseUri,
                                @Value("${resources.service.id}")
                                String serviceId,
                                DiscoveryClient discoveryClient,
                                CloseableHttpClient httpClient,
                                ObjectMapper objectMapper) {
        this.discoveryClient = discoveryClient;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.serviceId = serviceId;

        serviceUri = discoveryClient.getInstances(serviceId).stream()
                .findAny().map(ServiceInstance::getUri)
                .orElse(URI.create(baseUri));
    }

    @Override
    @Retryable(value = {Exception.class},
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${retry.delay}",
                    multiplierExpression = "${retry.multiply}",
                    maxDelayExpression = "${retry.maxDelay}"))
    public AudioShort save(AudioInput input) {
        try {
            HttpPost post = new HttpPost(serviceUri + RESOURCES_URL);
            post.addHeader("content-type", "application/json");
            post.setEntity(new StringEntity(objectMapper.writeValueAsString(input)));
            CloseableHttpResponse response = httpClient.execute(post);
            return objectMapper.readValue(response.getEntity().getContent(), AudioShort.class);
        } catch (IOException e) {
            log.warn("Unable to send audio input.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Retryable(value = {Exception.class},
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${retry.delay}",
                    multiplierExpression = "${retry.multiply}",
                    maxDelayExpression = "${retry.maxDelay}"))
    public void delete(String key) {
        try {
            HttpDelete delete = new HttpDelete(serviceUri + RESOURCES_DELETE_BY_KEY_URL);
            delete.setURI(new URIBuilder(delete.getURI())
                    .addParameter("key", key)
                    .build());

            httpClient.execute(delete);
        } catch (Exception e) {
            log.warn("Unable to delete by key {}", key, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void makePermanent(Long id) {
        try {
            HttpPatch patch = new HttpPatch(serviceUri + RESOURCES_MAKE_PERMANENT_URL + "/" + id);

            httpClient.execute(patch);
        } catch (Exception e) {
            log.warn("Unable to make permanent storage with id {}", id, e);
            throw new RuntimeException(e);
        }
    }

}
