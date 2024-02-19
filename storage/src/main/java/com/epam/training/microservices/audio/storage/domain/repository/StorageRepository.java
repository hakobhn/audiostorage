package com.epam.training.microservices.audio.storage.domain.repository;


import com.epam.training.microservices.audio.storage.domain.model.Storage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<Storage, Long> {

}
