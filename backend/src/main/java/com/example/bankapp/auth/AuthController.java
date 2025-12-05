package com.example.bankapp.auth;

import com.example.bankapp.security.JwtService;
import com.example.bankapp.user.User;
import com.example.bankapp.user.UserRepository;
import com.example.bankapp.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository resetRepo;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserService userService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          PasswordResetTokenRepository resetRepo) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resetRepo = resetRepo;
    }

    // ---- Request DTOs -------------------------------------------------------

    public static record SignupRequest(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String address,
            @NotBlank String phone,
            @Pattern(regexp = "\\d{7}", message = "SSN must be exactly 7 digits")
            String ssn7
    ) {}

    public static record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public static record ForgotPasswordRequest(
            @Email @NotBlank String email
    ) {}

    public static record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank String newPassword
    ) {}

    // ---- Endpoints ----------------------------------------------------------

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest req) {
        User u = new User();
        u.setEmail(req.email());
        u.setFirstName(req.firstName());
        u.setLastName(req.lastName());
        u.setAddress(req.address());
        u.setPhone(req.phone());
        u.setSsn7(req.ssn7());

        User saved = userService.register(u, req.password());

        Map<String, Object> body = Map.of(
                "message", "User registered",
                "userId", saved.getId()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException ex) {
            // Bad username/password -> 401 with JSON error body
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }

        String token = jwtService.generateToken(req.email());
        Map<String, Object> body = Map.of(
                "token", token,
                "issuedAt", Instant.now().toString()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {

        User u = userRepository.findByEmail(req.email()).orElse(null);
        if (u == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No user with that email"));
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUser(u);
        prt.setExpiresAt(Instant.now().plusSeconds(3600));
        resetRepo.save(prt);

        // Demo behaviour: return token in response.
        // In production we would send this via email instead.
        return ResponseEntity.ok(Map.of("resetToken", token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {

        PasswordResetToken prt = resetRepo.findByToken(req.token()).orElse(null);
        if (prt == null || prt.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or expired token"));
        }

        User u = prt.getUser();
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(u);
        resetRepo.deleteByToken(req.token());

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
}
