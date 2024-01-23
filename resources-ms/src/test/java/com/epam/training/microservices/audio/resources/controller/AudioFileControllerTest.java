package com.epam.training.microservices.audio.resources.controller;

import com.epam.training.microservices.audio.resources.ResourcesApplication;
import com.epam.training.microservices.audio.resources.dto.AudioMessage;
import com.epam.training.microservices.audio.resources.service.AudioFileService;
import com.epam.training.microservices.audio.resources.service.AudioQueueingService;
import com.epam.training.microservices.audio.resources.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.epam.training.microservices.audio.resources.controller.ControllerEndpoints.RESOURCES_URL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ResourcesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@SqlGroup({
        @Sql(value = "classpath:data/schema-audio.sql", executionPhase = BEFORE_TEST_METHOD),
        @Sql(value = "classpath:data/data-audio.sql", executionPhase = BEFORE_TEST_METHOD)
})
class AudioFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AudioFileService audioFileService;
    @MockBean
    private StorageService storageService;
    @MockBean
    private AudioQueueingService audioQueueingService;

    @Captor
    private ArgumentCaptor<AudioMessage> messageCaptor;

    @BeforeEach
    public void setUp() {
        when(storageService.store(anyString(), any(byte[].class))).thenReturn("dummy/path");
    }

    @Test
    void shouldAddNewAudioFile() throws Exception {
        // given
        Resource resource = resourceLoader.getResource("classpath:audio/Vanna-Rainelle---YAD-Яд-ENGLISH-VERSION.mp3");
        File audio = resource.getFile();

        byte[] data = Files.readAllBytes(audio.toPath());

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .multipart(RESOURCES_URL)
                        .file("file", data))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String actual = result.getResponse().getContentAsString();

        // then
        assertThat(actual).isEqualTo("Media has been successfully pushed to queue.");

        verify(audioQueueingService).sendMessage(messageCaptor.capture());
        AudioMessage value = messageCaptor.getValue();

        assertThat(value.getName()).isBlank();
        assertThat(value.getLocation()).isEqualTo("dummy/path");
        assertThat(value.getData()).isNotNull();
    }

    @Test
    void shouldRetrieveAudioById() throws Exception {
        // given
        RequestBuilder request = MockMvcRequestBuilders
                .get(RESOURCES_URL + "/1");

        when(storageService.read(anyString()))
                .thenReturn("Audio file".getBytes(StandardCharsets.UTF_8));

        // when
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/mpeg"))
                .andDo(print());
    }

    @Test
    void shouldDeleteById() throws Exception {
        // given
        RequestBuilder request = MockMvcRequestBuilders
                .delete(RESOURCES_URL)
                .param("id", "1,2");

        // when
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailToDeleteByNotExistId() throws Exception {
        // given
        RequestBuilder request = MockMvcRequestBuilders
                .delete(RESOURCES_URL)
                .param("id", "3,4");

        // when
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteByLocation() throws Exception {
        // given
        RequestBuilder request = MockMvcRequestBuilders
                .delete(RESOURCES_URL + "/deleteByKey")
                .param("key", "C:\\audio\\20240114223443_Vanna-Rainelle---YAD-Яд-ENGLISH-VERSION.mp3");

        // when
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotFailOnDeleteingByNotExistLocation() throws Exception {
        // given
        RequestBuilder request = MockMvcRequestBuilders
                .delete(RESOURCES_URL + "/deleteByKey")
                .param("key", "C:\\audio\\Not-exists-20240114223443_Vanna-Rainelle---YAD-Яд-ENGLISH-VERSION.mp3");

        // when
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk());
    }
}