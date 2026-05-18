package com.valorantcoach.controller;

import com.valorantcoach.dto.AuthRequest;
import com.valorantcoach.dto.AuthResponse;
import com.valorantcoach.model.User;
import com.valorantcoach.repository.UserRepository;
import com.valorantcoach.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          BCryptPasswordEncoder encoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email déjà utilisé.");
        }
        User user = new User(req.getEmail(), encoder.encode(req.getPassword()));
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), false));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        return userRepository.findByEmail(req.getEmail())
                .filter(u -> encoder.matches(req.getPassword(), u.getPassword()))
                .map(u -> ResponseEntity.ok(new AuthResponse(
                        jwtUtil.generateToken(u.getEmail()),
                        u.getEmail(),
                        u.isSubscribed())))
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.isValid(token)) return ResponseEntity.status(401).build();
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(new AuthResponse(token, u.getEmail(), u.isSubscribed())))
                .orElse(ResponseEntity.status(404).build());
    }
}
