package com.epam.training.microservices.audio.resource_processor.service;

import com.epam.training.microservices.audio.resource_processor.exception.UnsupportedFileFormatException;
import com.epam.training.microservices.audio.resource_processor.model.AudioInput;
import com.epam.training.microservices.audio.resource_processor.model.AudioMessage;
import com.epam.training.microservices.audio.resource_processor.model.AudioMetadata;
import com.epam.training.microservices.audio.resource_processor.model.AudioShort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ProcessorServiceTests {

    @Autowired
    private ProcessorService processorService;

    @Autowired
    private MetadataService metadataService;
    @MockBean
    private SongService songService;
    @MockBean
    private ResourcesService resourcesService;

    @Autowired
    private ResourceLoader resourceLoader;

    private AudioMessage audioMessage;

    @Captor
    private ArgumentCaptor<AudioInput> audioInputCaptor;
    @Captor
    private ArgumentCaptor<AudioMetadata> audioMetadataCaptor;

    @BeforeEach
    public void setUp() throws IOException {
        audioMessage = new AudioMessage();
        audioMessage.setName("testMessage");
        audioMessage.setLocation("testLocation");
    }

    @Test
    void shouldProcessGivenMessage() throws Exception {
        // given
        Resource resource = resourceLoader.getResource("classpath:audio/Vanna-Rainelle---YAD-Яд-ENGLISH-VERSION.mp3");
        File audio = resource.getFile();
        byte[] data = Files.readAllBytes(audio.toPath());
        audioMessage.setData(data);

        // when
        when(resourcesService.save(any(AudioInput.class), anyString())).thenReturn(new AudioShort(1L));
        processorService.processAudioFile(audioMessage, "testTraceId");

        // then
        verify(resourcesService).save(audioInputCaptor.capture(), anyString());
        AudioInput inputValue = audioInputCaptor.getValue();
        assertThat(inputValue.getName()).isEqualTo(audioMessage.getName());
        assertThat(inputValue.getLocation()).isEqualTo(audioMessage.getLocation());
        assertThat(inputValue.getBytes()).isEqualTo(audioMessage.getData().length);

        verify(songService).addSong(audioMetadataCaptor.capture(), anyString());
        AudioMetadata metadataValue = audioMetadataCaptor.getValue();
        assertThat(metadataValue.getResourceId()).isEqualTo(1L);
        assertThat(metadataValue.getName()).isEqualTo("Vanna Rainelle - YAD Яд ENGLISH VERSION(musicdownload.cc)");
        assertThat(metadataValue.getArtist()).isEqualTo("Vanna Rainelle");
        assertThat(metadataValue.getAlbum()).isEqualTo("English Mp3 Songs :: Musicdownload.cc");
        assertThat(metadataValue.getLength()).isEqualTo("181012.296875");
        assertThat(metadataValue.getYear()).isEqualTo("2023");
    }

    @Test
    void shouldFailOnProcessingNotValidMp3File() throws IOException {
        // given
        Resource resource = resourceLoader.getResource("classpath:audio/file_example_WAV_1MG.wav");
        File audio = resource.getFile();
        byte[] data = Files.readAllBytes(audio.toPath());
        audioMessage.setData(data);

        // when
        Exception exception = assertThrows(UnsupportedFileFormatException.class, () -> {
            processorService.processAudioFile(audioMessage, "testTraceId");
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("Not audio/mpeg file submitted");
        verify(resourcesService, times(1)).delete(audioMessage.getLocation(), "testTraceId");
    }
}
