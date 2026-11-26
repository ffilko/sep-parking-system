package rs.ac.uns.ftn.sep.parking.order;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.sep.parking.auth.ParkingUser;
import rs.ac.uns.ftn.sep.parking.auth.ParkingUserRepository;
import rs.ac.uns.ftn.sep.parking.vehicle.Vehicle;
import rs.ac.uns.ftn.sep.parking.zone.ParkingPriceResponse;
import rs.ac.uns.ftn.sep.parking.zone.ParkingPriceService;
import rs.ac.uns.ftn.sep.parking.zone.ParkingZone;
import rs.ac.uns.ftn.sep.parking.zone.ParkingZoneRepository;

@Service
public class ParkingOrderService {

    private final ParkingOrderRepository orders;
    private final ParkingUserRepository users;
    private final ParkingZoneRepository zones;
    private final ParkingPriceService prices;

    public ParkingOrderService(ParkingOrderRepository orders, ParkingUserRepository users,
                               ParkingZoneRepository zones, ParkingPriceService prices) {
        this.orders = orders;
        this.users = users;
        this.zones = zones;
        this.prices = prices;
    }

    @Transactional(readOnly = true)
    public ParkingOrderStatusResponse getStatus(long ownerId, Long orderId) {
        var order = orders.findStatusByIdAndOwner(orderId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking order not found"));
        var session = order.getStatus() == ParkingOrder.Status.PAID
                ? new ParkingOrderStatusResponse.Session(order.getSessionId(), order.getStartsAt(), order.getExpiresAt())
                : null;
        return new ParkingOrderStatusResponse(order.getId(), order.getStatus(), order.getZoneId(),
                order.getZoneName(), order.getVehicleId(), order.getRegistrationPlate(), order.getDurationMinutes(),
                order.getEffectiveDurationMinutes(), order.getBillableHours(),
                order.getTotalPrice(), order.getCurrency(), order.getCreatedAt(), session);
    }

    @Transactional
    public ParkingOrderResponse create(long ownerId, Long zoneId, Long vehicleId, int durationMinutes) {
        ParkingUser owner = users.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        ParkingPriceResponse price = prices.calculatePrice(zoneId, durationMinutes);
        // The price service loaded this zone in the same persistence context.
        ParkingZone zone = zones.getReferenceById(price.zoneId());
        Vehicle vehicle = orders.findOwnedVehicle(vehicleId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        ParkingOrder order = orders.save(new ParkingOrder(owner, zone, vehicle, price));
        return new ParkingOrderResponse(order.getId(), price.zoneId(), vehicleId, price.durationMinutes(),
                order.getEffectiveDurationMinutes(), price.billableHours(), order.getHourlyRate(), order.getTotalPrice(), order.getCurrency(),
                order.getStatus(), order.getCreatedAt());
    }
}
