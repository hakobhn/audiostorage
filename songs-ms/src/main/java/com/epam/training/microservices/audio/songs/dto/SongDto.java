package com.epam.training.microservices.audio.songs.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SongDto {
    private String id;
    @NotNull(message = "${song.resourceId.required}")
    private Long resourceId;
    private String name;
    private String artist;
    private String album;
    private String length;
    private String year;
}
