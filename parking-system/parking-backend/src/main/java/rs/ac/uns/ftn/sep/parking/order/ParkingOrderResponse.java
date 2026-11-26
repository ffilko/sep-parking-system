package rs.ac.uns.ftn.sep.parking.order;

import java.math.BigDecimal;
import java.time.Instant;

public record ParkingOrderResponse(
        Long id,
        Long zoneId,
        Long vehicleId,
        int durationMinutes,
        long effectiveDurationMinutes,
        int billableHours,
        BigDecimal hourlyRate,
        BigDecimal totalPrice,
        String currency,
        ParkingOrder.Status status,
        Instant createdAt
) {
}
