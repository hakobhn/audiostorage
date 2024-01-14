package com.epam.training.microservices.audio.songs.domain.repository;


import com.epam.training.microservices.audio.songs.domain.model.Song;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface SongRepository extends MongoRepository<Song, String> {

    Optional<Song> findByResourceId(Long resourceId);

}
