package com.epam.training.microservices.audio.resources.controller;

import com.epam.training.microservices.audio.resources.component.Tracer;
import com.epam.training.microservices.audio.resources.config.LocalizedMessageProvider;
import com.epam.training.microservices.audio.resources.dto.AudioDto;
import com.epam.training.microservices.audio.resources.dto.AudioInput;
import com.epam.training.microservices.audio.resources.dto.AudioMessage;
import com.epam.training.microservices.audio.resources.dto.AudioShort;
import com.epam.training.microservices.audio.resources.dto.StorageDetailsShort;
import com.epam.training.microservices.audio.resources.exception.BadRequestException;
import com.epam.training.microservices.audio.resources.exception.NotFoundException;
import com.epam.training.microservices.audio.resources.service.AudioFileService;
import com.epam.training.microservices.audio.resources.service.AudioQueueingService;
import com.epam.training.microservices.audio.resources.service.StorageDetailsService;
import com.epam.training.microservices.audio.resources.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.training.microservices.audio.resources.controller.ControllerEndpoints.RESOURCES_URL;
import static com.epam.training.microservices.audio.resources.util.StringUtils.encode;

@Slf4j
@RestController
@RolesAllowed({"ROLE_USER"})
@RequestMapping(value = RESOURCES_URL)
@RequiredArgsConstructor
public class AudioFileController {

    private final LocalizedMessageProvider messageProvider;
    private final AudioQueueingService audioQueueingService;
    private final AudioFileService audioFileService;
    private final StorageDetailsService storageDetailsService;
    private final Tracer tracer;

    @Autowired
    @Qualifier("AwsStorageService")
    private StorageService storageService;

    @Value("${aws.storage.bucket}")
    private String bucketName;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile uploadedFile) throws IOException {

        log.debug("Trace id: {}", tracer.traceId());

        log.debug("Received new audio content {}", uploadedFile.getOriginalFilename());
        byte[] data = uploadedFile.getBytes();

        String key = storageService.store(encode(uploadedFile.getOriginalFilename()), data);

        StorageDetailsShort detailsShort = storageDetailsService.saveStaging(bucketName, key);

        log.debug("Passing audio entry {} to processing", key);
        audioQueueingService.sendMessage(new AudioMessage(uploadedFile.getOriginalFilename(),
                key, detailsShort.getId(), data));
        return new ResponseEntity<>(messageProvider.getMessage("meda.pushed.success"), HttpStatus.CREATED);
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AudioShort> save(@Validated @RequestBody AudioInput audioInput) {
        try {
            AudioDto dto = audioFileService.create(audioInput);
            return ResponseEntity.ok(AudioShort.builder().id(dto.getId()).build());
        } catch (Exception e) {
            log.warn("Unable process file {}", audioInput.getName(), e);
            throw new RuntimeException("Unable to save file", e);
        }
    }

    @GetMapping(value = "/{id}", produces = "audio/mpeg")
    public HttpEntity<byte[]> download(@PathVariable("id") long id, HttpServletResponse response) throws IOException {
        AudioDto dto = audioFileService.getById(id);

        HttpHeaders headers = new HttpHeaders();
        response.setContentType("audio/mpeg");
        response.setContentLength(Long.valueOf(dto.getBytes()).intValue());
        response.setHeader("Content-Disposition", "attachment; filename=" + encode(dto.getName()));

        return new HttpEntity<byte[]>(storageService.read(dto.getLocation()), headers);
    }

    @PatchMapping(value = "/makePermanent/{id}")
    public ResponseEntity<?> makePermanent(@PathVariable("id") long id) {
        storageDetailsService.makePermanent(id);
        return ResponseEntity.ok().build();
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
                            audioQueueingService.deleteMessage(id);
                            removedIds.add(dto.getId());
                            storageService.delete(dto.getLocation());
                            return dto.getLocation();
                        })
                        .orElseThrow(() -> new NotFoundException(id + " not found media"))
        );
        return ResponseEntity.ok(Map.of("ids", removedIds));
    }

    @DeleteMapping(path = "deleteByKey")
    ResponseEntity<?> deleteByKey(@RequestParam(name = "key") String key) {
        audioFileService.deleteByLocation(key);
        storageService.delete(key);
        return ResponseEntity.ok().build();
    }

}
