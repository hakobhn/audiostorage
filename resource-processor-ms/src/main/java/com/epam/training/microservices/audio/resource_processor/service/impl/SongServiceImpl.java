package com.epam.training.microservices.audio.resource_processor.service.impl;

import com.epam.training.microservices.audio.resource_processor.model.AudioMetadata;
import com.epam.training.microservices.audio.resource_processor.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private static final String SONGS_URL = "/songs";
    private static final String SONGS_DELETE_BY_RESOURCE_URL = SONGS_URL + "/deleteByResource";

    @Value("${songs.ms.base.uri}")
    private String baseUri;

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    @Retryable(value = { Exception.class },
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${retry.delay}",
                    multiplierExpression = "${retry.multiply}",
                    maxDelayExpression = "${retry.maxDelay}"))
    public void addSong(AudioMetadata metadata) throws IOException {
        HttpPost post = new HttpPost(baseUri + SONGS_URL);
        post.addHeader("content-type", "application/json");
        post.setEntity(new StringEntity(objectMapper.writeValueAsString(metadata)));

        httpClient.execute(post);
    }

    @Override
    @Retryable(value = { Exception.class },
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${retry.delay}",
                    multiplierExpression = "${retry.multiply}",
                    maxDelayExpression = "${retry.maxDelay}"))
    public void deleteSong(Long resourceId) {
        try {
            HttpDelete delete = new HttpDelete(baseUri + SONGS_DELETE_BY_RESOURCE_URL);
            delete.setURI(new URIBuilder(delete.getURI())
                    .addParameter("resourceId", String.valueOf(resourceId))
                    .build());

            httpClient.execute(delete);
        } catch (Exception e) {
            log.warn("Unable to delete song.", e);
            throw new RuntimeException(e);
        }
    }

}
