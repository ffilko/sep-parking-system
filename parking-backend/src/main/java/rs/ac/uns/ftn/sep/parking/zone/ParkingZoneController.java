package rs.ac.uns.ftn.sep.parking.zone;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ParkingZoneController {

    private final ParkingZoneRepository repository;
    private final ParkingPriceService priceService;

    public ParkingZoneController(ParkingZoneRepository repository, ParkingPriceService priceService) {
        this.repository = repository;
        this.priceService = priceService;
    }

    @GetMapping("/api/parking-zones/{zoneId}/price")
    public ParkingPriceResponse getParkingPrice(
            @PathVariable("zoneId") Long zoneId,
            @RequestParam("durationMinutes") int durationMinutes) {
        return priceService.calculatePrice(zoneId, durationMinutes);
    }

    @GetMapping("/api/parking-zones")
    public List<ParkingZoneResponse> getParkingZones() {
        return repository.findAll(Sort.by("name", "id")).stream()
                .map(zone -> new ParkingZoneResponse(
                        zone.getId(),
                        zone.getName(),
                        zone.getHourlyRate(),
                        zone.getCurrency(),
                        zone.getMinDurationMinutes(),
                        zone.getMaxDurationMinutes()))
                .toList();
    }
}
