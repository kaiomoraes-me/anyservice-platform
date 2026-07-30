package com.anyservice.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/** Stores uploaded files (avatars) on the local filesystem. */
@Service
public class FileStorageService {

    private final String uploadDir = "uploads/avatars";

    public FileStorageService() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads", e);
        }
    }

    /** Saves an avatar image with a unique UUID filename and returns the relative URL path. */
    public String saveAvatar(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Falha ao guardar um arquivo vazio.");
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null
                    ? originalFileName.substring(originalFileName.lastIndexOf("."))
                    : ".jpg";
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path destinationFile = Paths.get(uploadDir).resolve(uniqueFileName).normalize().toAbsolutePath();
            file.transferTo(destinationFile);

            return "/uploads/avatars/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo", e);
        }
    }
}
