package com.epam.training.microservices.audio.resources.domain.repository;


import com.epam.training.microservices.audio.resources.domain.model.AudioFile;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AudioFileRepository extends CrudRepository<AudioFile, Long> {

    @Modifying
    @Query("delete from AudioFile af where af.location=:location")
    void deleteByLocation(@Param("location") String location);
}
