package com.anyservice.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded files (avatars) on the local filesystem.
 * The upload directory is /app/uploads/avatars inside the Docker container,
 * backed by a persistent Docker volume to survive container rebuilds.
 */
@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads/avatars";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final Path uploadPath;

    public FileStorageService() {
        this.uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads: " + this.uploadPath, e);
        }
    }

    /**
     * Saves an avatar image with a unique UUID filename and returns the relative URL path.
     * Validates file size, content type, and extension before saving.
     */
    public String saveAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("O arquivo enviado está vazio.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("O arquivo excede o tamanho máximo de 5MB.");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new RuntimeException("Formato de arquivo não suportado. Use: JPG, JPEG, PNG, GIF ou WEBP.");
        }

        try {
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path destinationFile = this.uploadPath.resolve(uniqueFileName).normalize();

            // Security: prevent path traversal attacks
            if (!destinationFile.startsWith(this.uploadPath)) {
                throw new RuntimeException("Caminho de arquivo inválido.");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/avatars/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo: " + e.getMessage(), e);
        }
    }
}
