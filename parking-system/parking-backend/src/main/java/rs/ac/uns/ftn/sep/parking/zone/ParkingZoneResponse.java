package rs.ac.uns.ftn.sep.parking.zone;

import java.math.BigDecimal;

public record ParkingZoneResponse(
        Long id,
        String name,
        BigDecimal hourlyRate,
        String currency,
        int minDurationMinutes,
        int maxDurationMinutes
) {
}
