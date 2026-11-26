package rs.ac.uns.ftn.sep.parking.order;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.sep.parking.auth.JwtService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
public class ParkingSessionController {

    private final JwtService jwtService;
    private final ParkingSessionRepository sessions;

    public ParkingSessionController(JwtService jwtService, ParkingSessionRepository sessions) {
        this.jwtService = jwtService;
        this.sessions = sessions;
    }

    @GetMapping("/api/parking-sessions/active")
    public ResponseEntity<List<ActiveSessionResponse>> listActive(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        long ownerId = jwtService.requireUserId(authorization);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        List<ActiveSessionResponse> result = sessions.findActiveByOwner(ownerId, now).stream()
                .map(session -> new ActiveSessionResponse(
                        session.getId(), session.getZoneId(), session.getZoneName(),
                        session.getVehicleId(), session.getRegistrationPlate(),
                        session.getStartsAt(), session.getExpiresAt(),
                        session.getPaidAmount(), session.getCurrency(),
                        Duration.between(now, session.getExpiresAt()).getSeconds()))
                .toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    @GetMapping("/api/parking-sessions/history")
    public ResponseEntity<List<HistoricalSessionResponse>> listHistory(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        long ownerId = jwtService.requireUserId(authorization);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        List<HistoricalSessionResponse> result = sessions.findHistoryByOwner(ownerId, now).stream()
                .map(session -> new HistoricalSessionResponse(
                        session.getId(), session.getZoneId(), session.getZoneName(),
                        session.getVehicleId(), session.getRegistrationPlate(),
                        session.getStartsAt(), session.getExpiresAt(),
                        session.getPaidAmount(), session.getCurrency(), session.getPspTransactionId()))
                .toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    public record HistoricalSessionResponse(
            Long id,
            Long zoneId,
            String zoneName,
            Long vehicleId,
            String registrationPlate,
            Instant startsAt,
            Instant expiresAt,
            BigDecimal paidAmount,
            String currency,
            String pspTransactionId
    ) {
    }

    public record ActiveSessionResponse(
            Long id,
            Long zoneId,
            String zoneName,
            Long vehicleId,
            String registrationPlate,
            Instant startsAt,
            Instant expiresAt,
            BigDecimal paidAmount,
            String currency,
            long remainingSeconds
    ) {
    }
}
