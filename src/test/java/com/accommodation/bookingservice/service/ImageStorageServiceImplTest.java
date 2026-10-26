package com.accommodation.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class ImageStorageServiceImplTest {
    private ImageStorageServiceImpl imageStorageService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        imageStorageService = new ImageStorageServiceImpl();
        tempDir = Files.createTempDirectory("test-uploads-");
        ReflectionTestUtils.setField(imageStorageService, "uploadDir", tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    @DisplayName("store: valid jpeg file stores successfully and returns relative URL")
    void storeValidFileSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4}
        );

        String url = imageStorageService.store(file);

        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/"));
        assertTrue(url.endsWith(".jpg"));

        String filename = url.substring("/uploads/".length());
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    @DisplayName("store: empty file throws IllegalArgumentException")
    void storeEmptyFileThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[]{}
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> imageStorageService.store(file)
        );
        assertEquals("File cannot be empty", ex.getMessage());
    }

    @Test
    @DisplayName("store: unsupported mime type throws IllegalArgumentException")
    void storeUnsupportedMimeTypeThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.pdf",
                "application/pdf",
                new byte[]{1, 2, 3}
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> imageStorageService.store(file)
        );
        assertEquals("Invalid file format. Allowed: JPEG, PNG, WEBP, GIF", ex.getMessage());
    }

    @Test
    @DisplayName("store: unsupported extension throws IllegalArgumentException")
    void storeUnsupportedExtensionThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.exe",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> imageStorageService.store(file)
        );
        assertTrue(ex.getMessage().contains("Invalid file extension"));
    }

    @Test
    @DisplayName("delete: deletes existing file successfully")
    void deleteExistingFileSuccess() throws IOException {
        Path testFile = tempDir.resolve("sample.jpg");
        Files.write(testFile, new byte[]{1, 2, 3});
        assertTrue(Files.exists(testFile));

        imageStorageService.delete("/uploads/sample.jpg");

        assertFalse(Files.exists(testFile));
    }

    @Test
    @DisplayName("delete: ignores remote http/https URLs")
    void deleteRemoteUrlIgnored() {
        imageStorageService.delete("https://images.unsplash.com/photo-12345");
        // No exception thrown, treated as no-op
    }
}
