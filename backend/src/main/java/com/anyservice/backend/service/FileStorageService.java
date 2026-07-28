package com.anyservice.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // Em produção, isso seria um Bucket AWS S3. Para testes, salvamos numa pasta local.
    private final String uploadDir = "uploads/avatars";

    public FileStorageService() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads", e);
        }
    }

    public String saveAvatar(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Falha ao guardar um arquivo vazio.");
            }
            
            // Gerar um nome único para o arquivo para evitar conflitos
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : ".jpg";
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            Path destinationFile = Paths.get(uploadDir).resolve(Paths.get(uniqueFileName)).normalize().toAbsolutePath();
            
            // Salvar no disco
            file.transferTo(destinationFile);
            
            // Retornar a URL relativa que o front vai acessar
            return "/uploads/avatars/" + uniqueFileName;
            
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo", e);
        }
    }
}
