package com.accommodation.bookingservice.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ImageStorageServiceImpl implements ImageStorageService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + extension;

        Path targetDir = getUploadPath();
        Path destination = targetDir.resolve(uniqueFilename).normalize().toAbsolutePath();

        if (!destination.startsWith(targetDir)) {
            throw new IllegalArgumentException("Cannot store file outside current directory");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored accommodation image: {}", destination);
            return "/uploads/" + uniqueFilename;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + uniqueFilename, e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()
                || imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return;
        }

        String filename = imageUrl.startsWith("/uploads/")
                ? imageUrl.substring("/uploads/".length())
                : imageUrl;

        Path targetDir = getUploadPath();
        Path filePath = targetDir.resolve(filename).normalize().toAbsolutePath();

        if (!filePath.startsWith(targetDir)) {
            log.warn("Attempt to delete file outside upload directory: {}", imageUrl);
            return;
        }

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Deleted previous accommodation image: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", filePath, e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the allowed limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid file format. Allowed: JPEG, PNG, WEBP, GIF");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file extension: " + extension);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    private Path getUploadPath() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new IllegalStateException("Could not initialize upload folder: " + path, e);
            }
        }
        return path;
    }
}
