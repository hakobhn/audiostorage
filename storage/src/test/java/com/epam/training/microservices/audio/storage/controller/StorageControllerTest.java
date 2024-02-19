package com.epam.training.microservices.audio.storage.controller;


import com.epam.training.microservices.audio.storage.StorageApplication;
import com.epam.training.microservices.audio.storage.domain.repository.StorageRepository;
import com.epam.training.microservices.audio.storage.dto.StorageShort;
import com.epam.training.microservices.audio.storage.mapper.StorageMapper;
import com.epam.training.microservices.audio.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static com.epam.training.microservices.audio.storage.controller.ControllerEndpoints.STORAGE_URL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@SqlGroup({
        @Sql(value = "classpath:data/schema-storage.sql", executionPhase = BEFORE_TEST_METHOD)
})
class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private StorageRepository storageRepository;

    @BeforeEach
    public void setUp() {
    }

    @Test
    void shouldSuccessfullyAddNewStorageFromJsonString() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String messageStr = Files.readString(Paths.get(Objects.requireNonNull(
                classLoader.getResource("json/sample-storage.json")).toURI()));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(STORAGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        StorageShort actual = objectMapper.readValue(result.getResponse().getContentAsString(), StorageShort.class);

        // then
        assertThat(actual.getId()).isPositive();
        assertThat(storageRepository.findById(actual.getId()).isPresent());
    }

}