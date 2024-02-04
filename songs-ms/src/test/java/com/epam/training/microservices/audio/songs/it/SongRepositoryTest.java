package com.epam.training.microservices.audio.songs.it;

import com.epam.training.microservices.audio.songs.SongsApplication;
import com.epam.training.microservices.audio.songs.domain.model.Song;
import com.epam.training.microservices.audio.songs.domain.repository.SongRepository;
import de.flapdoodle.embed.mongo.commands.MongoImportArguments;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ExecutedMongoImportProcess;
import de.flapdoodle.embed.mongo.transitions.MongoImport;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Start;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.event.annotation.AfterTestClass;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataMongo
@SpringBootTest(classes = SongsApplication.class)
class SongRepositoryTest {

    @Autowired
    private SongRepository songRepository;

    private TransitionWalker.ReachedState<RunningMongodProcess> mongoDProcess;
    private TransitionWalker.ReachedState<ExecutedMongoImportProcess> mongoImportProcess;

    @BeforeAll
    public void setUp() {
        String os = System.getProperty("os.name");
        ClassLoader classLoader = SongRepositoryTest.class.getClassLoader();
        String jsonFile = Objects.requireNonNull(
                classLoader.getResource("entity/songs.json")).getPath();

        if (os != null && os.toLowerCase().contains("windows")) {
            jsonFile = jsonFile.substring(1);
        }

        Version.Main version = Version.Main.PRODUCTION;

        MongoImportArguments arguments = MongoImportArguments.builder()
                .databaseName("songs")
                .collectionName("songs")
                .importFile(jsonFile)
                .isJsonArray(true)
                .upsertDocuments(true)
                .build();

        mongoDProcess = Mongod.builder()
                .net(Start.to(Net.class).initializedWith(Net.defaults().withPort(27019)))
                .build()
                .transitions(version)
                .walker()
                .initState(StateID.of(RunningMongodProcess.class));

        Transitions mongoImportTransitions = MongoImport.instance()
                .transitions(version)
                .replace(Start.to(MongoImportArguments.class).initializedWith(arguments))
                .addAll(Start.to(ServerAddress.class).initializedWith(mongoDProcess.current().getServerAddress()));

        mongoImportProcess = mongoImportTransitions.walker().initState(StateID.of(ExecutedMongoImportProcess.class));
    }

    @AfterTestClass
    public void tearDownAfterTestClass() {
        mongoImportProcess.close();
        mongoDProcess.close();
    }

    @Test
    void findById_ReturnsSong_WhenSongExists() {
        Song song = songRepository.findByResourceId(1L).orElse(null);
        assertThat(song).isNotNull();
        assertThat(song.getId()).isEqualTo("65b66878dd7f3f73e86bc860");
        assertThat(song.getAlbum()).isEqualTo("Bohemian Rhapsody (The Original Soundtrack)");
        assertThat(song.getLength()).isEqualTo("272304.0");
        assertThat(song.getYear()).isEqualTo("2018");
    }

    @Test
    void findById_ReturnsNull_WhenSongDoesNotExist() {
        Song song = songRepository.findByResourceId(-1L).orElse(null);
        assertThat(song).isNull();
    }

    @Test
    void findAll_ReturnsAllSongsInPageableOrder() {
        Page<Song> page = songRepository
                .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "year")));
        assertThat(page.getTotalElements()).isEqualTo(4);

        List<Song> songList = page.getContent();
        assertThat(songList).hasSize(4);
        assertThat(songList.get(0).getYear()).isEqualTo("1996");
        assertThat(songList.get(1).getYear()).isEqualTo("2016");
        assertThat(songList.get(2).getYear()).isEqualTo("2018");
        assertThat(songList.get(3).getYear()).isEqualTo("2023");
    }
}
