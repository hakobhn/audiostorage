package com.epam.training.microservices.audio.resources.it;

import com.epam.training.microservices.audio.resources.ResourcesApplication;
import com.epam.training.microservices.audio.resources.dto.AudioDto;
import com.epam.training.microservices.audio.resources.dto.AudioInput;
import com.epam.training.microservices.audio.resources.exception.NotFoundException;
import com.epam.training.microservices.audio.resources.service.AudioFileService;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = ResourcesApplication.class)
class AudioDaoIntegrationTest implements PostgresTestContainer {

    @Autowired
    private AudioFileService audioFileService;

    @Test
    @Order(1)
    @Commit
    void create_test() {
        // given
        AudioInput audioInput = AudioInput.builder()
                .name("Vanna-Rainelle---YAD-??-ENGLISH-VERSION.mp3")
                .location("storage\\20240128175754_Vanna-Rainelle---YAD-%D0%AF%D0%B4-ENGLISH-VERSION.mp3")
                .bytes(3006186)
                .build();

        // when
        AudioDto actual = audioFileService.create(audioInput);

        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo("Vanna-Rainelle---YAD-??-ENGLISH-VERSION.mp3");
    }

    @Test
    @Order(2)
    void delete_test_withExistingResourceId() {
        // given
        AudioInput audioInput = AudioInput.builder()
                .name("Vanna-Rainelle---YAD-??-ENGLISH-VERSION.mp3")
                .name("storage\\20240128175754_Vanna-Rainelle---YAD-%D0%AF%D0%B4-ENGLISH-VERSION.mp3")
                .bytes(3006186)
                .build();

        // when
        AudioDto actual = audioFileService.create(audioInput);
        assertThat(audioFileService.getById(actual.getId())).isNotNull();
        audioFileService.delete(actual.getId());

        //then
        Exception exception = assertThrows(NotFoundException.class, () -> {
            audioFileService.getById(actual.getId());
        });

        assertThat(exception.getMessage()).isEqualTo("Audio file with id 2, not found.");
    }
}
