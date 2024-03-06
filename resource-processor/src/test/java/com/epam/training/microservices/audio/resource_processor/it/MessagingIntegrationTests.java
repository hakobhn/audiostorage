package com.epam.training.microservices.audio.resource_processor.it;

import com.epam.training.microservices.audio.resource_processor.model.AudioInput;
import com.epam.training.microservices.audio.resource_processor.model.AudioMessage;
import com.epam.training.microservices.audio.resource_processor.model.AudioShort;
import com.epam.training.microservices.audio.resource_processor.service.StorageDetailsService;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

import static com.epam.training.microservices.audio.resource_processor.component.TracingConstants.AUTH_HEADER;
import static com.epam.training.microservices.audio.resource_processor.component.TracingConstants.CURRENT_TRACE_ID_HEADER;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.shaded.org.hamcrest.core.Is.is;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class MessagingIntegrationTests implements RabbitTestContainer {

    @MockBean
    private DiscoveryClient discoveryClient;

    private static final ClientAndServer mockServerClient = startClientAndServer();
    private static final String RESOURCES_URL = "/resources";
    private static final String RESOURCES_SAVE_URL = "/resources/save";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @MockBean
    private StorageDetailsService storageDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${audio.rabbitmq.add.exchange}")
    private String exchangeAudioQueue;
    @Value("${audio.rabbitmq.add.routing_key}")
    private String addRoutingKey;

    @DynamicPropertySource
    static void registerResourceMsProperties(DynamicPropertyRegistry registry) {
        registry.add("resources.ms.base.uri", () -> "http://localhost:" + mockServerClient.getPort());
    }

    @BeforeAll
    public static void setUp() {
        mockServerClient.reset();
    }

    @AfterAll
    public static void stopMockServer() {
        mockServerClient.stop();
    }

    private void createExpectationForAudioInputProcessing(String name, String location, int bytes) throws Exception {
        AudioInput audioInput = AudioInput.builder()
                .name(name)
                .location(location)
                .bytes(bytes)
                .build();
        AudioShort audioShort = new AudioShort(1L);
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(RESOURCES_SAVE_URL)
                                .withHeaders(
                                    Header.header("content-type", "application/json"),
                                    Header.header("x-current-trace-id", "testTraceId"),
                                    Header.header("AUTHORIZATION", "testAccessToken")
                                )
                                .withBody(objectMapper.writeValueAsString(audioInput))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatus.SC_OK)
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(objectMapper.writeValueAsString(audioShort))
                );
    }

    @Test
    void onNewValidAudioEvent_test(CapturedOutput output) throws Exception {

        String name = "Vanna-Rainelle---YAD-??-ENGLISH-VERSION.mp3";
        String location = "storage\\20240128175754_Vanna-Rainelle---YAD-%D0%AF%D0%B4-ENGLISH-VERSION.mp3";

        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setName(name);
        audioMessage.setLocation(location);

        Resource resource = resourceLoader.getResource("classpath:audio/Vanna-Rainelle---YAD-Яд-ENGLISH-VERSION.mp3");
        File audio = resource.getFile();
        byte[] data = Files.readAllBytes(audio.toPath());
        audioMessage.setData(data);

        createExpectationForAudioInputProcessing(name, location, data.length);

        rabbitTemplate.convertAndSend(exchangeAudioQueue, addRoutingKey, audioMessage, m -> {
                m.getMessageProperties().getHeaders().put(CURRENT_TRACE_ID_HEADER, "testTraceId");
                m.getMessageProperties().getHeaders().put(AUTH_HEADER, List.of("testAccessToken"));
        return m;
        });

        await().atMost(Duration.ofSeconds(60))
                .until(messageReceived(output), is(true));

        assertThat(output.getOut()).contains(
                "Received new message",
                "Processing message"
        );

        verify(storageDetailsService).makePermanent(any(), eq("testTraceId"));

        mockServerClient
                .verify(
                        request()
                                .withMethod("POST")
                                .withPath(RESOURCES_SAVE_URL)
                );
    }

    @Test
    void onNewInvalidAudioEvent_test(CapturedOutput output) throws IOException {

        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setName("testMessage");
        audioMessage.setLocation("testLocation");

        Resource resource = resourceLoader.getResource("classpath:audio/file_example_WAV_1MG.wav");
        File audio = resource.getFile();
        byte[] data = Files.readAllBytes(audio.toPath());
        audioMessage.setData(data);

        rabbitTemplate.convertAndSend(exchangeAudioQueue, addRoutingKey, audioMessage, m -> {
            m.getMessageProperties().getHeaders().put(CURRENT_TRACE_ID_HEADER, "testTraceId");
            m.getMessageProperties().getHeaders().put(AUTH_HEADER, List.of("testAccessToken"));
            return m;
        });

        await().atMost(Duration.ofSeconds(60))
                .until(invalidMessageReceived(output), is(true));

        assertThat(output.getOut()).contains(
                "Not valid audio file with location testLocation"
        );
    }

    private Callable<Boolean> messageReceived(CapturedOutput output) {
        return () -> output.getOut().contains("Sending add new song request to songs microservice.");
    }

    private Callable<Boolean> invalidMessageReceived(CapturedOutput output) {
        return () -> output.getOut().contains("Not valid audio file with location");
    }
}
