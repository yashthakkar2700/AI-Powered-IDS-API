package idsapi.com.example.idsapi.controller;

import idsapi.com.example.idsapi.model.User;
import idsapi.com.example.idsapi.repository.UserRepository;
import idsapi.com.example.idsapi.service.UserService;
import idsapi.com.example.idsapi.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;   // Inject UserService

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        return ResponseEntity.ok(savedUser);
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User dbUser = userService.getUserByUsername(user.getUsername());

        if (dbUser != null && userService.checkPassword(user.getPassword(), dbUser.getPassword())) {
            String token = jwtService.generateToken(dbUser.getUsername());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", Long.toString(dbUser.getId()));
            return ResponseEntity.ok(response);
        } else {
            throw new RuntimeException("Invalid username or password");
        }
    }

    @GetMapping("/profile")
    public String profile(@RequestHeader(value="Authorization", required=false) String authHeader) {
        System.out.println("AuthController::profile(): Profile Auth header: " + authHeader);
        String token = authHeader.substring(7);
        String username = jwtService.validateTokenAndGetUsername(token);
        return "Hello, " + username + "! This is your profile.";
    }
}
