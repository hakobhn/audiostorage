package com.epam.training.microservices.audio.resources.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

@Slf4j
public class AddAudioStepDefinitionTest extends AbstractSpringConfigurationTest {

    private byte[] audioData;
    private ResponseEntity<String> response = null;

    @Given("^the audio file with data$")
    public void the_audio_with_data() throws Throwable {
        if (log.isInfoEnabled()) {
            log.info("Audio to be saved");
        }
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("audio/Vanna-Rainelle---YAD-Яд-ENGLISH-VERSION.mp3");
        audioData = IOUtils.toByteArray(inputStream);
    }

    @When("^the client calls \"([^\"]*)\"$")
    public void the_client_calls_audio_save(String path) throws Throwable {

        if (log.isInfoEnabled()) {
            log.info("path {}", path);
        }
        String url = buildUrl(HOST, PORT, path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("file", audioData);

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        response = invokeRESTCall(url, HttpMethod.POST, requestEntity);
    }

    @Then("^the client receives status code of (\\d+)$")
    public void the_client_receives_status_code_of(int statusCode) throws Throwable {

        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            assertEquals(statusCode, response.getStatusCode().value());
        }
    }

    @Then("^the response contains message \"([^\"]*)\"$")
    public void the_response_contains_message(String message) throws Throwable {

        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            String responseStr = mapper.readValue(responseBody, String.class);
            assertEquals(responseStr, message);
        }
    }

}
