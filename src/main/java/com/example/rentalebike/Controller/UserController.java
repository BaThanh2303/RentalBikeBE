package com.example.rentalebike.Controller;


import com.example.rentalebike.Models.User;
import com.example.rentalebike.Service.UserService;
import com.example.rentalebike.Services.StorageService;
import com.example.rentalebike.dto.UserGmailCccdResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final StorageService storageService;

    public UserController(UserService userService, StorageService storageService) {
        this.userService = userService;
        this.storageService = storageService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Get all users with Gmail and CCCD image data
     * GET /api/users/gmail-cccd
     * Returns: List of users with userId, name, email (Gmail), and cccdImageUrl
     */
    @GetMapping("/gmail-cccd")
    public ResponseEntity<Map<String, Object>> getAllUsersGmailCccd(HttpServletRequest request) {
        try {
            List<User> users = userService.getAllUsers();

            // Get base URL for constructing full image URLs
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

            List<UserGmailCccdResponse> userResponses = users.stream()
                    .map(user -> {
                        String fullImageUrl = null;
                        if (user.getCccdImageUrl() != null && !user.getCccdImageUrl().isEmpty()) {
                            // Convert relative path to full URL
                            fullImageUrl = baseUrl + "/uploads" + user.getCccdImageUrl();
                        }

                        return UserGmailCccdResponse.builder()
                                .userId(user.getUserId())
                                .name(user.getName())
                                .email(user.getEmail()) // Gmail address
                                .cccdImageUrl(fullImageUrl) // Full CCCD image URL
                                .build();
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách người dùng thành công");
            response.put("totalUsers", userResponses.size());
            response.put("users", userResponses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh sách người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long userId) {
        try {
            User user = userService.getUserInfo(userId);

            Map<String, Object> userInfo = Map.of(
                "userId", user.getUserId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "role", user.getRole().toString()
            );

            return ResponseEntity.ok(userInfo);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = Map.of(
                "message", "Không tìm thấy thông tin người dùng"
            );
            return ResponseEntity.status(404).body(errorResponse);
        }
    }

    /**
     * Upload CCCD image for a user
     * POST /api/users/{id}/cccd
     * Security: Only logged-in user can upload their own CCCD
     */
    @PostMapping("/{id}/cccd")
    public ResponseEntity<Map<String, Object>> uploadCccd(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // TODO: Add authentication check - only user can upload their own CCCD
            // For now, we'll proceed without authentication

            // Validate user exists
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Store the file
            String cccdImageUrl = storageService.storeCccdFile(file, id);

            // Update user's CCCD image URL
            userService.updateUserCccdImageUrl(id, cccdImageUrl);

            response.put("message", "CCCD uploaded successfully");
            response.put("cccdImageUrl", cccdImageUrl);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            response.put("error", "Failed to store file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Get CCCD image for a user
     * GET /api/users/{id}/cccd
     * Security: Only admin can view CCCD of any user
     */
    @GetMapping("/{id}/cccd")
    public ResponseEntity<Resource> getCccd(@PathVariable Long id) {
        try {
            // TODO: Add authentication check - only admin can view CCCD of any user
            // For now, we'll proceed without authentication

            // Get user and their CCCD image URL
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String cccdImageUrl = user.getCccdImageUrl();
            if (cccdImageUrl == null || cccdImageUrl.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Extract filename from URL path
            String filename = cccdImageUrl.substring(cccdImageUrl.lastIndexOf('/') + 1);

            // Load file as Resource
            Resource file = storageService.loadCccdFile(filename);

            // Determine content type
            String contentType = "application/octet-stream";
            if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(file);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
