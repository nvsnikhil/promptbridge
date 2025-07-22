package com.nikhil.promptbridge.controller;

import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.service.JwtService;
import com.nikhil.promptbridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// DTO for Registration Request
class RegisterRequest {
    public String name;
    public String email;
    public String password;
}

// DTO for Login Request
class LoginRequest {
    public String email;
    public String password;
}

// DTO for Login Response
class LoginResponse {
    private final String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) {
        userService.registerUser(request.name, request.email, request.password);
        return ResponseEntity.ok("User registered successfully!");
    }

    // v-- ADD THIS NEW LOGIN METHOD --v
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest request) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email, request.password)
        );

        // If authentication is successful, generate a token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        // Return the token in the response
        return ResponseEntity.ok(new LoginResponse(token));
    }
}