package rs.ac.uns.ftn.sep.parking.zone;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class ParkingPriceService {

    private final ParkingZoneRepository repository;

    public ParkingPriceService(ParkingZoneRepository repository) {
        this.repository = repository;
    }

    public ParkingPriceResponse calculatePrice(Long zoneId, int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new InvalidDurationException("Duration must be positive");
        }

        ParkingZone zone = repository.findById(zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking zone not found"));

        if (durationMinutes < zone.getMinDurationMinutes()) {
            throw new InvalidDurationException("Requested duration must be at least "
                    + zone.getMinDurationMinutes() + " minutes");
        }

        int billableHours = Math.ceilDiv(durationMinutes, 60);
        long effectiveDurationMinutes = billableHours * 60L;
        if (effectiveDurationMinutes > zone.getMaxDurationMinutes()) {
            throw new InvalidDurationException("Rounded parking duration of "
                    + effectiveDurationMinutes + " minutes exceeds the zone maximum of "
                    + zone.getMaxDurationMinutes() + " minutes");
        }
        BigDecimal totalPrice = zone.getHourlyRate().multiply(BigDecimal.valueOf(billableHours));
        return new ParkingPriceResponse(zone.getId(), durationMinutes, effectiveDurationMinutes,
                billableHours, totalPrice, zone.getCurrency());
    }

    static class InvalidDurationException extends ResponseStatusException {

        InvalidDurationException(String reason) {
            super(HttpStatus.BAD_REQUEST, reason);
        }
    }
}
