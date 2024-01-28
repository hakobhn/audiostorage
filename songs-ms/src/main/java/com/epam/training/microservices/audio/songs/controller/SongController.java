package com.epam.training.microservices.audio.songs.controller;

import com.epam.training.microservices.audio.songs.dto.SongDto;
import com.epam.training.microservices.audio.songs.dto.SongShort;
import com.epam.training.microservices.audio.songs.exception.BadRequestException;
import com.epam.training.microservices.audio.songs.service.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

import static com.epam.training.microservices.audio.songs.controller.ControllerEndpoints.SONGS_URL;

@Slf4j
@RestController
@RequestMapping(value = SONGS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SongShort> create(@Valid @RequestBody SongDto songDto) {
        log.info("Received add new song request.");
        songDto = songService.create(songDto);
        return ResponseEntity.ok(new SongShort(songDto.getId()));
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<SongDto> get(@PathVariable("id") String id) {
        return ResponseEntity.ok(songService.getById(id));
    }

    @DeleteMapping
    ResponseEntity<Map<String, List<Long>>> delete(@RequestParam("id") String idsStr) {
        List<Integer> ids = Arrays.stream(idsStr.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        if (ids.size() > 200) {
            throw new BadRequestException("Too many ids submitted");
        }
        List<Long> removedIds = new ArrayList<>();
        ids.forEach(
                id -> songService.findByResourceId(id)
                        .ifPresent(dto -> {
                            removedIds.add(dto.getResourceId());
                            songService.delete(dto.getId());
                        })
        );
        return ResponseEntity.ok(Map.of("ids", removedIds));
    }

    @DeleteMapping(path = "deleteByResource")
    ResponseEntity<?> deleteByResource(@RequestParam(name = "resourceId") Long resourceId) {
        songService.deleteByResourceId(resourceId);
        return ResponseEntity.ok().build();
    }

}
