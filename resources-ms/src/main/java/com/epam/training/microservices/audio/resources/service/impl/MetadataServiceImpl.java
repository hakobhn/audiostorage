package com.epam.training.microservices.audio.resources.service.impl;

import com.epam.training.microservices.audio.resources.dto.AudioMetadata;
import com.epam.training.microservices.audio.resources.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class MetadataServiceImpl implements MetadataService {

    @Override
    public AudioMetadata extract(byte[] bytes) {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();

        try (InputStream input = new ByteArrayInputStream(bytes)){
            Parser parser = new Mp3Parser();
            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
        } catch (TikaException | SAXException | IOException e) {
            log.warn("Unable to extract metadata.", e);
        }

        return AudioMetadata.builder()
                .name(metadata.get("dc:title"))
                .artist(metadata.get("xmpDM:artist"))
                .album(metadata.get("xmpDM:album"))
                .length(metadata.get("xmpDM:duration"))
                .year(metadata.get("xmpDM:releaseDate"))
                .build();
    }

    public String detectContentType(byte[] bytes) {
        Detector detector = new DefaultDetector();
        Metadata metadata = new Metadata();

        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            MediaType mediaType = detector.detect(stream, metadata);
            return mediaType.toString();
        } catch (IOException e) {
            log.warn("Unable to detect byte array content type", e);
        }
        return null;
    }
}
