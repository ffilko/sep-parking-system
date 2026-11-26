package rs.ac.uns.ftn.sep.parking.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final ParkingUserRepository users;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());

    public AuthService(ParkingUserRepository users) {
        this.users = users;
    }

    ParkingUser register(String email, String password) {
        validatePasswordLength(password);
        if (users.existsByEmail(email)) {
            throw duplicateEmail();
        }

        ParkingUser user = new ParkingUser(email, passwordEncoder.encode(password), Instant.now());
        try {
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            if (users.existsByEmail(email)) {
                throw duplicateEmail();
            }
            throw exception;
        }
    }

    ParkingUser authenticate(String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw invalidCredentials();
        }

        ParkingUser user = users.findByEmail(email).orElse(null);
        String hash = user == null ? dummyPasswordHash : user.getPasswordHash();
        if (!passwordEncoder.matches(password, hash) || user == null) {
            throw invalidCredentials();
        }
        return user;
    }

    private static void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password exceeds 72 UTF-8 bytes");
        }
    }

    private static ResponseStatusException duplicateEmail() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
