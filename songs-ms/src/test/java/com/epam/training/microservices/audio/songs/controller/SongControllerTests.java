package com.epam.training.microservices.audio.songs.controller;

import com.epam.training.microservices.audio.songs.SongsApplication;
import com.epam.training.microservices.audio.songs.domain.model.Song;
import com.epam.training.microservices.audio.songs.domain.repository.SongRepository;
import com.epam.training.microservices.audio.songs.dto.SongDto;
import com.epam.training.microservices.audio.songs.dto.SongShort;
import com.epam.training.microservices.audio.songs.mapper.SongMapper;
import com.epam.training.microservices.audio.songs.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import static com.epam.training.microservices.audio.songs.controller.ControllerEndpoints.SONGS_URL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SongsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SongControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SongService songService;

    @Autowired
    private SongMapper songMapper;
    @MockBean
    private SongRepository songRepository;

    @BeforeEach
    public void setUp() {
        Song songEntity = new Song();
        songEntity.setId(UUID.randomUUID().toString());
        songEntity.setName("Dummy song");
        when(songRepository.save(any(Song.class))).thenReturn(songEntity);
    }

    @Test
    void shouldSuccessfullyAddNewSongFromObject() throws Exception {
        // given
        SongDto songDto = new SongDto();
        songDto.setResourceId(1L);
        songDto.setName("Vanna Rainelle - YAD Яд ENGLISH VERSION(musicdownload.cc)");
        songDto.setArtist("Vanna Rainelle");
        songDto.setAlbum("English Mp3 Songs :: Musicdownload.cc");
        songDto.setLength("181012.296875");
        songDto.setYear("2023");

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(SONGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(songDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        SongShort actual = objectMapper.readValue(result.getResponse().getContentAsString(), SongShort.class);

        // then
        assertThat(actual.getId()).isNotBlank();
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @Test
    void shouldSuccessfullyAddNewSongFromJsonString() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String messageStr = Files.readString(Paths.get(Objects.requireNonNull(
                classLoader.getResource("input/sample-song.json")).toURI()));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(SONGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        SongShort actual = objectMapper.readValue(result.getResponse().getContentAsString(), SongShort.class);

        // then
        assertThat(actual.getId()).isNotBlank();
    }

    @Test
    void shouldFailToAddNewSongWithoutResourceId() throws Exception {
        // given
        SongDto songDto = new SongDto();
        songDto.setName("Vanna Rainelle - YAD Яд ENGLISH VERSION(musicdownload.cc)");
        songDto.setArtist("Vanna Rainelle");
        songDto.setAlbum("English Mp3 Songs :: Musicdownload.cc");
        songDto.setLength("181012.296875");
        songDto.setYear("2023");

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .post(SONGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(songDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        verify(songRepository, never()).save(any(Song.class));
    }

}
