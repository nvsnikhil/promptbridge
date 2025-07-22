package com.nikhil.promptbridge.service;

import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        
        // We hash the password before saving it to the database
        user.setPasswordHash(passwordEncoder.encode(password));
        
        // The createdAt field will be set by the database default
        
        return userRepository.save(user);
    }
}