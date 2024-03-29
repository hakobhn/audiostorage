package com.epam.training.microservices.audio.resource_processor.service.impl;

import com.epam.training.microservices.audio.resource_processor.exception.UnsupportedFileFormatException;
import com.epam.training.microservices.audio.resource_processor.model.AudioInput;
import com.epam.training.microservices.audio.resource_processor.model.AudioMessage;
import com.epam.training.microservices.audio.resource_processor.model.AudioMetadata;
import com.epam.training.microservices.audio.resource_processor.model.AudioShort;
import com.epam.training.microservices.audio.resource_processor.service.MetadataService;
import com.epam.training.microservices.audio.resource_processor.service.ProcessorService;
import com.epam.training.microservices.audio.resource_processor.service.ResourcesService;
import com.epam.training.microservices.audio.resource_processor.service.SongService;
import com.epam.training.microservices.audio.resource_processor.service.StorageDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessorServiceImpl implements ProcessorService {

    private final MetadataService metadataService;
    private final SongService songService;
    private final ResourcesService resourcesService;
    private final StorageDetailsService storageDetailsService;

    @Override
    public void processAudioFile(AudioMessage message) {
        log.info("Processing message {}", message.getLocation());
        byte[] data = message.getData();
        if (!metadataService.detectContentType(data).equalsIgnoreCase("audio/mpeg")) {
            resourcesService.delete(message.getLocation());
            throw new UnsupportedFileFormatException("Not audio/mpeg file submitted");
        }

        try {
            AudioMetadata metadata = metadataService.extract(message.getData());

            storageDetailsService.makePermanent(message.getDetailsId());

            AudioShort audioShort = resourcesService.save(AudioInput.builder()
                    .name(message.getName())
                    .location(message.getLocation())
                    .bytes(data.length)
                    .build());

            metadata.setResourceId(audioShort.getId());
            log.info("Sending add new song request to songs microservice.");
            songService.addSong(metadata);
        } catch (Exception e) {
            log.warn("Unable to process file {}", message.getName(), e);
            resourcesService.delete(message.getLocation());
        }

    }

    @Override
    public void deleteAudioFile(Long resourceId) {
        try {
            songService.deleteSong(resourceId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
