package com.epam.training.microservices.audio.storage.controller;


import com.epam.training.microservices.audio.storage.dto.StorageDto;
import com.epam.training.microservices.audio.storage.dto.StorageShort;
import com.epam.training.microservices.audio.storage.exception.BadRequestException;
import com.epam.training.microservices.audio.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.training.microservices.audio.storage.controller.ControllerEndpoints.STORAGE_URL;

@Slf4j
@RestController
@RequestMapping(value = STORAGE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StorageShort> create(@Valid @RequestBody StorageDto storageDto) {
        log.info("Received add new storage request.");
        storageDto = storageService.create(storageDto);
        return ResponseEntity.ok(new StorageShort(storageDto.getId()));
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<StorageDto> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(storageService.getById(id));
    }

    @PatchMapping(path = "/{id}")
    ResponseEntity<StorageShort> makePermanent(@PathVariable Long id) {
        log.info("Received make permanent storage request.");
        storageService.makePermanent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<List<StorageDto>> getStorages() {
            return new ResponseEntity<>(storageService.getAll(), HttpStatus.OK);
    }

    @DeleteMapping
    ResponseEntity<Map<String, List<Long>>> delete(@RequestParam("id") String idsStr) {
        List<Long> ids = Arrays.stream(idsStr.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        if (ids.size() > 200) {
            throw new BadRequestException("Too many ids submitted");
        }
        List<Long> removedIds = new ArrayList<>();
        ids.forEach(
                id -> storageService.findById(id)
                        .ifPresent(dto -> {
                            storageService.delete(dto.getId());
                            removedIds.add(dto.getId());
                        })
        );
        return ResponseEntity.ok(Map.of("ids", removedIds));
    }

}
