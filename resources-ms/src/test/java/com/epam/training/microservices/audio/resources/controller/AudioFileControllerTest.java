package com.epam.training.microservices.audio.resources.controller;

import com.epam.training.microservices.audio.resources.ResourcesApplication;
import com.epam.training.microservices.audio.resources.dto.AudioMetadata;
import com.epam.training.microservices.audio.resources.dto.AudioShort;
import com.epam.training.microservices.audio.resources.service.AudioFileService;
import com.epam.training.microservices.audio.resources.service.MetadataService;
import com.epam.training.microservices.audio.resources.service.SongService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
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
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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
    @Autowired
    private MetadataService metadataService;
    @MockBean
    private StorageService storageService;
    @MockBean
    private SongService songService;

    @Captor
    private ArgumentCaptor<AudioMetadata> metadataCaptor;

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
                .andExpect(status().isOk())
                .andReturn();

        AudioShort actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), AudioShort.class);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isGreaterThan(0);

        verify(songService).addSong(metadataCaptor.capture());
        AudioMetadata value = metadataCaptor.getValue();

        assertThat(value.getName()).isEqualTo("Vanna Rainelle - YAD Яд ENGLISH VERSION(musicdownload.cc)");
        assertThat(value.getArtist()).isEqualTo("Vanna Rainelle");
        assertThat(value.getAlbum()).isEqualTo("English Mp3 Songs :: Musicdownload.cc");
        assertThat(value.getLength()).isEqualTo("181012.296875");
        assertThat(value.getYear()).isEqualTo("2023");
    }

    @Test
    void shouldFailToAddInvalidAudio() throws Exception {
        // given
        Resource resource = resourceLoader.getResource("classpath:audio/file_example_WAV_1MG.wav");
        File audio = resource.getFile();

        byte[] data = Files.readAllBytes(audio.toPath());

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(RESOURCES_URL)
                        .file("file", data))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRetrieveAudioById() throws Exception {
        // given
        RequestBuilder request = MockMvcRequestBuilders
                .get(RESOURCES_URL + "/1");

        when(storageService.read(anyString()))
                .thenReturn(new ByteArrayInputStream("Audio file".getBytes(StandardCharsets.UTF_8)));

        // when
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andDo(print());
    }
}