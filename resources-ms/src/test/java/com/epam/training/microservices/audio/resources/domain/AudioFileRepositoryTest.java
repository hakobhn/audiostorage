package com.epam.training.microservices.audio.resources.domain;

import com.epam.training.microservices.audio.resources.domain.model.AudioFile;
import com.epam.training.microservices.audio.resources.domain.repository.AudioFileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AudioFileRepositoryTest {

    @Autowired
    private AudioFileRepository audioFileRepository;

    @AfterEach
    void cleanUp() {
        this.audioFileRepository.deleteAll();
    }

    @Test
    void shouldReturnListOfPostWithMatchingAuthor() {
        AudioFile firstFile = new AudioFile();
        firstFile.setName("firstFile");
        firstFile.setLocation("/dummy/path");
        this.audioFileRepository.save(firstFile);

        AudioFile secondFile = new AudioFile();
        secondFile.setName("secondFile");
        secondFile.setLocation("/dummy/second/path");
        this.audioFileRepository.save(secondFile);

        Iterable<AudioFile> audioFiles = audioFileRepository.findAll();

        assertThat(audioFiles).hasSize(2);
    }

}