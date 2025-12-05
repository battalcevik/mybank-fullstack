package com.example.bankapp;

import com.example.bankapp.user.User;
import com.example.bankapp.user.UserRepository;
import com.example.bankapp.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTests {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void register_encodesPassword_and_createsUser() {
        User u = new User();
        u.setEmail("test133@bank.local");
        u.setFirstName("Test");
        u.setLastName("User");
        u.setAddress("123 Main");
        u.setPhone("555-1111");
        u.setSsn7("7654321");
        User saved = userService.register(u, "Secret123!");
        assertNotNull(saved.getId());
        assertTrue(passwordEncoder.matches("Secret123!", saved.getPasswordHash()));
    }
}
