package rs.ac.uns.ftn.sep.parking.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private static final long TOKEN_LIFETIME_SECONDS = 3600;
    private static final String ISSUER = "parking-backend";
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public JwtService(@Value("${parking.auth.jwt-secret:${PARKING_JWT_SECRET:}}") String configuredSecret) {
        if (configuredSecret.isBlank()) {
            encoder = null;
            decoder = null;
            return;
        }
        SecretKeySpec key = signingKey(configuredSecret);
        encoder = NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator(ISSUER)));
        decoder = jwtDecoder;
    }

    TokenResponse issueToken(Long userId) {
        if (encoder == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "JWT signing is not configured");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(TOKEN_LIFETIME_SECONDS))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", TOKEN_LIFETIME_SECONDS);
    }

    public long requireUserId(String authorization) {
        if (decoder == null || authorization == null
                || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw invalidToken();
        }
        try {
            var jwt = decoder.decode(authorization.substring(7).strip());
            long userId = Long.parseLong(jwt.getSubject());
            if (jwt.getExpiresAt() == null || userId <= 0) {
                throw invalidToken();
            }
            return userId;
        } catch (JwtException | IllegalArgumentException exception) {
            throw invalidToken();
        }
    }

    private static ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing bearer token");
    }

    private static SecretKeySpec signingKey(String configuredSecret) {
        byte[] secret;
        try {
            secret = Base64.getDecoder().decode(configuredSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("JWT signing key must be Base64-encoded");
        }
        if (secret.length < 32) {
            throw new IllegalArgumentException("JWT signing key must contain at least 32 decoded bytes");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    }
}
