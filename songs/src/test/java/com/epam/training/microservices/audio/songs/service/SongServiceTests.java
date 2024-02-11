package com.epam.training.microservices.audio.songs.service;


import com.epam.training.microservices.audio.songs.dto.SongDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class SongServiceTests {

    @Autowired
    private SongService songService;

    @Test
    void shouldStoreNewSongIntoDb() {
        // given
        SongDto songDto = new SongDto();
        songDto.setName("Test song");
        songDto.setLength("length");
        songDto.setArtist("artist");
        songDto.setAlbum("album");
        songDto.setResourceId(10L);
        songDto.setYear("2018");

        // when
        SongDto actual = songService.create(songDto);

        // then
        assertThat(actual.getId()).isNotBlank();
    }

}
