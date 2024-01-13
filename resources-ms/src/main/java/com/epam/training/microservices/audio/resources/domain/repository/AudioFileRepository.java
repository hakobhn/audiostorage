package com.epam.training.microservices.audio.resources.domain.repository;


import com.epam.training.microservices.audio.resources.domain.model.AudioFile;
import org.springframework.data.repository.CrudRepository;

public interface AudioFileRepository extends CrudRepository<AudioFile, Long> {

}
