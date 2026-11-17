package com.uxinterfacely.experienceletter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalFileStorageService {
    private final Path root;
    public LocalFileStorageService(@Value("${app.file-storage-dir}") String directory) throws Exception {
        root = Paths.get(directory).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }
    public String save(MultipartFile file, String employeeId) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("PDF file is required");
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "letter.pdf" : file.getOriginalFilename());
        if (!original.toLowerCase().endsWith(".pdf")) throw new IllegalArgumentException("Only PDF files are allowed");
        String name = employeeId + "_" + UUID.randomUUID() + ".pdf";
        Path target = root.resolve(name).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid file name");
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return name;
    }
    public Resource load(String filename) throws Exception {
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root) || !Files.exists(target)) throw new NoSuchFileException("PDF not found");
        return new UrlResource(target.toUri());
    }
}
