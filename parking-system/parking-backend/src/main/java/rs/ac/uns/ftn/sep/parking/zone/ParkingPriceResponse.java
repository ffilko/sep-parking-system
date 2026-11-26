package rs.ac.uns.ftn.sep.parking.zone;

import java.math.BigDecimal;

public record ParkingPriceResponse(
        Long zoneId,
        int durationMinutes,
        long effectiveDurationMinutes,
        int billableHours,
        BigDecimal totalPrice,
        String currency
) {
}
