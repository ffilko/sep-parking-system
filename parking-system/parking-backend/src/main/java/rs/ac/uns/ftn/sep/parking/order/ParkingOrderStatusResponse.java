package rs.ac.uns.ftn.sep.parking.order;

import java.math.BigDecimal;
import java.time.Instant;

public record ParkingOrderStatusResponse(
        Long id,
        ParkingOrder.Status status,
        Long zoneId,
        String zoneName,
        Long vehicleId,
        String registrationPlate,
        int durationMinutes,
        long effectiveDurationMinutes,
        int billableHours,
        BigDecimal totalPrice,
        String currency,
        Instant createdAt,
        Session session
) {
    public record Session(Long id, Instant startsAt, Instant expiresAt) {
    }
}
