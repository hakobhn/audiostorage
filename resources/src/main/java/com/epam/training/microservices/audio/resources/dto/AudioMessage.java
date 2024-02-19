package com.epam.training.microservices.audio.resources.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AudioMessage {
    private String name;
    private String location;
    private Long detailsId;
    private byte[] data;
}
