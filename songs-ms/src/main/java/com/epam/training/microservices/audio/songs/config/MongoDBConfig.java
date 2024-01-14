package com.epam.training.microservices.audio.songs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.epam.training.microservices.audio.songs.domain.repository")
@Configuration
public class MongoDBConfig {

}
