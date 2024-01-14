package com.epam.training.microservices.audio.resources.service.impl;

import com.epam.training.microservices.audio.resources.dto.AudioMetadata;
import com.epam.training.microservices.audio.resources.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.epam.training.microservices.audio.resources.controller.ControllerEndpoints.SONGS_URL;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    @Value("${songs.ms.base.uri}")
    private String songsMsBaseUri;

    private final RestTemplate restTemplate;

    public void addSong(AudioMetadata metadata) {
        restTemplate.postForEntity(songsMsBaseUri + SONGS_URL, metadata, AudioMetadata.class);
    }

}
