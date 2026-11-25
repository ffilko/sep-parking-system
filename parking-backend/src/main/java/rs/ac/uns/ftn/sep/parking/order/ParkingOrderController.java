package rs.ac.uns.ftn.sep.parking.order;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.sep.parking.auth.JwtService;

@RestController
public class ParkingOrderController {

    static final String PATH = "/api/parking-orders";
    static final String OWNER_ID_ATTRIBUTE = "parkingOrderOwnerId";

    private final ParkingOrderService orders;
    private final JwtService jwtService;

    public ParkingOrderController(ParkingOrderService orders, JwtService jwtService) {
        this.orders = orders;
        this.jwtService = jwtService;
    }

    @GetMapping(PATH + "/{orderId}")
    public ParkingOrderStatusResponse getStatus(
            @PathVariable("orderId") Long orderId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return orders.getStatus(jwtService.requireUserId(authorization), orderId);
    }

    @PostMapping(PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingOrderResponse create(
            @RequestAttribute(OWNER_ID_ATTRIBUTE) long ownerId,
            @Valid @RequestBody CreateParkingOrderRequest request) {
        return orders.create(ownerId, request.zoneId(), request.vehicleId(), request.durationMinutes());
    }

    public record CreateParkingOrderRequest(
            @NotNull Long zoneId,
            @NotNull Long vehicleId,
            @NotNull @Positive Integer durationMinutes
    ) {
    }
}
