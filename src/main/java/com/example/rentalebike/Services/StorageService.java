package com.example.rentalebike.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class StorageService {

    @Value("${app.upload.dir:/uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String CCCD_FOLDER = "cccd";

    /**
     * Store CCCD file for a specific user
     * @param file The uploaded file
     * @param userId The user ID
     * @return The relative path where the file was stored
     * @throws IOException If file storage fails
     * @throws IllegalArgumentException If file validation fails
     */
    public String storeCccdFile(MultipartFile file, Long userId) throws IOException {
        validateFile(file);

        // Create directory if it doesn't exist
        Path cccdDir = Paths.get(uploadDir, CCCD_FOLDER);
        Files.createDirectories(cccdDir);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("%d_%s_cccd.%s", userId, timestamp, extension);

        // Store file
        Path targetLocation = cccdDir.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path
        return "/" + CCCD_FOLDER + "/" + filename;
    }

    /**
     * Load CCCD file as Resource
     * @param filename The filename to load
     * @return Resource object for the file
     * @throws IOException If file cannot be found or loaded
     */
    public Resource loadCccdFile(String filename) throws IOException {
        try {
            Path file = Paths.get(uploadDir, CCCD_FOLDER).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("File not found or not readable: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new IOException("File not found: " + filename, ex);
        }
    }

    /**
     * Delete CCCD file
     * @param relativePath The relative path of the file to delete
     * @return true if file was deleted, false if file didn't exist
     */
    public boolean deleteCccdFile(String relativePath) {
        try {
            if (relativePath == null || relativePath.isEmpty()) {
                return false;
            }

            // Remove leading slash if present
            String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            Path file = Paths.get(uploadDir).resolve(cleanPath);

            return Files.deleteIfExists(file);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Validate uploaded file
     * @param file The file to validate
     * @throws IllegalArgumentException If validation fails
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only JPG and PNG files are allowed");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/jpeg") && !contentType.startsWith("image/png"))) {
            throw new IllegalArgumentException("Invalid file type. Only JPG and PNG images are allowed");
        }
    }

    /**
     * Extract file extension from filename
     * @param filename The filename
     * @return The file extension without the dot
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }
}