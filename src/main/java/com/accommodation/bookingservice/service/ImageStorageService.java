package com.accommodation.bookingservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    String store(MultipartFile file);

    void delete(String imageUrl);
}
