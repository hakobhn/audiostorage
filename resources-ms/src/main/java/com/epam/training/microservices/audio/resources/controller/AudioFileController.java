package com.epam.training.microservices.audio.resources.controller;

import com.epam.training.microservices.audio.resources.dto.AudioDto;
import com.epam.training.microservices.audio.resources.dto.AudioInput;
import com.epam.training.microservices.audio.resources.dto.AudioMetadata;
import com.epam.training.microservices.audio.resources.dto.AudioShort;
import com.epam.training.microservices.audio.resources.exception.BadRequestException;
import com.epam.training.microservices.audio.resources.exception.UnsupportedFileFormatException;
import com.epam.training.microservices.audio.resources.service.AudioFileService;
import com.epam.training.microservices.audio.resources.service.MetadataService;
import com.epam.training.microservices.audio.resources.service.SongService;
import com.epam.training.microservices.audio.resources.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.training.microservices.audio.resources.controller.ControllerEndpoints.RESOURCES_URL;

@Slf4j
@RestController
@RequestMapping(value = RESOURCES_URL)
@RequiredArgsConstructor
public class AudioFileController {

    private final AudioFileService audioFileService;
    private final StorageService storageService;
    private final MetadataService metadataService;
    private final SongService songService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AudioShort> upload(@RequestParam("file") MultipartFile uploadedFile) throws IOException {

        byte[] data = uploadedFile.getBytes();

        if (!"audio/mpeg".equalsIgnoreCase(metadataService.detectContentType(data))) {
            throw new UnsupportedFileFormatException("Validation failed or request body is invalid MP3");
        }

        String location = storageService.store(uploadedFile.getOriginalFilename(), data);
        AudioMetadata metadata = metadataService.extract(data);

        AudioInput audioInput = AudioInput.builder()
                .name(uploadedFile.getOriginalFilename())
                .location(location)
                .bytes(uploadedFile.getSize())
                .build();
        AudioDto dto = null;
        try {
            dto = audioFileService.create(audioInput);
            metadata.setResourceId(dto.getId());
            songService.addSong(metadata);
            return ResponseEntity.ok(AudioShort.builder().id(dto.getId()).build());
        } catch (Exception e) {
            log.warn("Unable process file {}", uploadedFile.getOriginalFilename(), e);
            Optional.ofNullable(dto)
                    .ifPresent(d -> audioFileService.delete(d.getId()));
            storageService.delete(location);
            throw new RuntimeException("Unable to store file", e);
        }
    }

    @GetMapping(value = "/{id}", produces = "audio/mpeg")
    public HttpEntity<byte[]> download(@PathVariable("id") long id, HttpServletResponse response) throws IOException {
        AudioDto dto = audioFileService.getById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        response.setContentType("audio/mpeg");
        response.setContentLength(Long.valueOf(dto.getBytes()).intValue());
        response.setHeader("Content-Disposition", "attachment; filename=" + dto.getName());

        return new HttpEntity<byte[]>(IOUtils.toByteArray(storageService.read(dto.getLocation())), headers);
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, List<Long>>> delete(@RequestParam("id") String idsStr) {
        List<Integer> ids = Arrays.stream(idsStr.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        if (ids.size() > 200) {
            throw new BadRequestException("Too many ids submitted");
        }
        List<Long> removedIds = new ArrayList<>();
        ids.forEach(
                id -> audioFileService.findById(id)
                        .map(dto -> {
                            audioFileService.delete(id);
                            removedIds.add(dto.getId());
                            return dto.getLocation();
                        })
                        .ifPresent(storageService::delete)

        );
        return ResponseEntity.ok(Map.of("ids", removedIds));
    }

}
