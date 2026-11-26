package rs.ac.uns.ftn.sep.parking.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody Credentials credentials) {
        ParkingUser user = authService.register(credentials.email(), credentials.password());
        return new RegistrationResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }

    @PostMapping("/login")
    public ResponseEntity<JwtService.TokenResponse> login(@Valid @RequestBody Credentials credentials) {
        ParkingUser user = authService.authenticate(credentials.email(), credentials.password());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(jwtService.issueToken(user.getId()));
    }

    public record Credentials(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank String password
    ) {
        public Credentials {
            if (email != null) {
                email = email.strip().toLowerCase(Locale.ROOT);
            }
        }

        @Override
        public String toString() {
            return "Credentials[redacted]";
        }
    }

    public record RegistrationResponse(Long id, String email, Instant createdAt) {
    }
}
