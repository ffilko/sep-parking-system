package rs.ac.uns.ftn.sep.parking.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "parking_users")
public class ParkingUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ParkingUser() {
    }

    ParkingUser(String email, String passwordHash, Instant createdAt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    Long getId() {
        return id;
    }

    String getEmail() {
        return email;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
