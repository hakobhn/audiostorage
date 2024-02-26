package com.epam.training.microservices.audio.resource_processor.service.impl;

import com.epam.training.microservices.audio.resource_processor.model.AudioMetadata;
import com.epam.training.microservices.audio.resource_processor.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
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

import java.net.URI;

import static com.epam.training.microservices.audio.resource_processor.component.TracingConstants.CURRENT_TRACE_ID_HEADER;

@Slf4j
@Service
public class SongServiceImpl implements SongService {

    private static final String SONGS_URL = "/songs";
    private static final String SONGS_DELETE_BY_RESOURCE_URL = SONGS_URL + "/deleteByResource";

    private final String serviceId;

    private final DiscoveryClient discoveryClient;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final URI serviceUri;

    public SongServiceImpl(@Value("${songs.ms.base.uri}")
                           String baseUri,
                           @Value("${songs.service.id}")
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
    public void addSong(AudioMetadata metadata, String traceId) throws Exception {
        HttpPost post = new HttpPost(serviceUri + SONGS_URL);
        post.addHeader("content-type", "application/json");
        post.addHeader(CURRENT_TRACE_ID_HEADER, traceId);
        post.setEntity(new StringEntity(objectMapper.writeValueAsString(metadata)));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            log.info("Song sent response {}", response.getStatusLine());
        } catch (Exception e) {
            log.warn("Unable to store new song in songs microservice");
            throw e;
        }
    }

    @Override
    @Retryable(value = {Exception.class},
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${retry.delay}",
                    multiplierExpression = "${retry.multiply}",
                    maxDelayExpression = "${retry.maxDelay}"))
    public void deleteSong(Long resourceId, String traceId) throws Exception {
        HttpDelete delete = new HttpDelete(serviceUri + SONGS_DELETE_BY_RESOURCE_URL);
        delete.addHeader(CURRENT_TRACE_ID_HEADER, traceId);
        delete.setURI(new URIBuilder(delete.getURI())
                .addParameter("resourceId", String.valueOf(resourceId))
                .build());

        try (CloseableHttpResponse response = httpClient.execute(delete)) {
            log.info("Song delete request sent response {}", response.getStatusLine());
        } catch (Exception e) {
            log.warn("Unable to delete song.", e);
            throw e;
        }
    }

}
