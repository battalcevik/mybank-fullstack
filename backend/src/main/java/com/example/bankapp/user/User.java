package com.example.bankapp.user;

import jakarta.validation.constraints.Pattern;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import jakarta.persistence.Convert;
import com.example.bankapp.security.EncryptedStringConverter;


@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String passwordHash;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Convert(converter = EncryptedStringConverter.class)
    private String address;

    @NotBlank
    @Convert(converter = EncryptedStringConverter.class)
    private String phone;

    @Pattern(regexp = "\\d{7}", message = "SSN must be exactly 7 digits")
    @Convert(converter = EncryptedStringConverter.class)
    private String ssn7;

    private String roles = "USER";

    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSsn7() { return ssn7; }
    public void setSsn7(String ssn7) { this.ssn7 = ssn7; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Account> getAccounts() { return accounts; }
}
