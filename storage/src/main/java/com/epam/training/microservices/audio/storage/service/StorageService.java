package com.epam.training.microservices.audio.storage.service;

import com.epam.training.microservices.audio.storage.common.model.StorageType;
import com.epam.training.microservices.audio.storage.domain.model.Storage;
import com.epam.training.microservices.audio.storage.domain.repository.StorageRepository;
import com.epam.training.microservices.audio.storage.dto.StorageDto;
import com.epam.training.microservices.audio.storage.exception.NotFoundException;
import com.epam.training.microservices.audio.storage.mapper.StorageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class StorageService {

    private final StorageRepository storageRepository;
    private final StorageMapper storageMapper;

    public StorageDto create(StorageDto dto) {
        Storage entity = storageMapper.dtoToEntity(dto);
        entity = storageRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    public StorageDto getById(Long id) {
        return storageMapper.entityToDto(storageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Storage with id " + id + ", not found.")));
    }

    public Optional<StorageDto> findById(Long id) {
        return storageRepository.findById(id).map(storageMapper::entityToDto);
    }

    public List<StorageDto> getAll() {
        return storageRepository.findAll().stream().map(
                storageMapper::entityToDto).collect(Collectors.toList());
    }

    public void makePermanent(Long id) {
        storageRepository.findById(id).ifPresent(
                storage -> {
                    storage.setType(StorageType.PERMANENT);
                    storageRepository.save(storage);
                }
        );
    }

    public void delete(Long id) {
        storageRepository.deleteById(id);
    }

}
