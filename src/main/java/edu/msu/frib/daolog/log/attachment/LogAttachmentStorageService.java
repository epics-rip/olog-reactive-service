package edu.msu.frib.daolog.log.attachment;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface LogAttachmentStorageService {

    public void init();

    public String store(MultipartFile file);

    public Stream<Path> loadAll();

    public Path load(String filename);

    public Resource loadAsResource(String filename);

    void deleteAll();
}
