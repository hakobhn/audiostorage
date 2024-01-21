package com.epam.training.microservices.audio.resource_processor.service.impl;

import com.epam.training.microservices.audio.resource_processor.model.AudioInput;
import com.epam.training.microservices.audio.resource_processor.model.AudioShort;
import com.epam.training.microservices.audio.resource_processor.service.ResourcesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
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

@Service
@RequiredArgsConstructor
public class ResourcesServiceImpl implements ResourcesService {

    private static final String RESOURCES_URL = "/resources";
    private static final String RESOURCES_DELETE_BY_KEY_URL = RESOURCES_URL + "/deleteByKey";

    @Value("${resources.ms.base.uri}")
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
    public AudioShort save(AudioInput input) {
        try {
            HttpPost post = new HttpPost(baseUri + RESOURCES_URL);
            post.addHeader("content-type", "application/json");
            post.setEntity(new StringEntity(objectMapper.writeValueAsString(input)));
            CloseableHttpResponse response = httpClient.execute(post);
            return objectMapper.readValue(response.getEntity().getContent(), AudioShort.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Retryable(value = { Exception.class },
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${retry.delay}",
                    multiplierExpression = "${retry.multiply}",
                    maxDelayExpression = "${retry.maxDelay}"))
    public void delete(String key) {
        try {
            HttpDelete delete = new HttpDelete(baseUri + RESOURCES_DELETE_BY_KEY_URL);
            delete.setURI( new URIBuilder(delete.getURI())
                    .addParameter("key", key)
                    .build());

            httpClient.execute(delete);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
