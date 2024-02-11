package com.epam.training.microservices.audio.songs.domain.repository;


import com.epam.training.microservices.audio.songs.domain.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface SongRepository extends MongoRepository<Song, String> {

    Optional<Song> findByResourceId(Long resourceId);
    Page<Song> findByNameContaining(String name, Pageable page);

}
