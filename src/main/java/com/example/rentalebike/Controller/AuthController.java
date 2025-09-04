package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.User;
import com.example.rentalebike.Repository.UserRepository;
import com.example.rentalebike.Security.CustomUserDetails;
import com.example.rentalebike.config.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UserDetailsService userDetailsService,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        if (user.getRole() == null) {
            user.setRole(User.Role.valueOf("USER"));
        }
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginData) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginData.get("email"), loginData.get("password"))
        );

        User user = userRepository.findByEmail(loginData.get("email"))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(new CustomUserDetails(user));

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", String.valueOf(user.getRole()));   // ✅ thêm role
        response.put("name", user.getName());   // có thể thêm name để FE hiển thị
        response.put("email", user.getEmail());
        return ResponseEntity.ok(response);
    }



}
