package rs.ac.uns.ftn.sep.parking.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
public class PaymentCallbackController {

    static final String PATH = "/api/parking-orders/payment-callback";

    private final PaymentCallbackService payments;

    public PaymentCallbackController(PaymentCallbackService payments) {
        this.payments = payments;
    }

    @PostMapping(PATH)
    public PaymentCallbackResponse confirm(@Valid @RequestBody PaymentCallbackRequest request) {
        return payments.confirm(request);
    }

    public enum PaymentStatus {
        SUCCESS,
        FAILED
    }

    public record PaymentCallbackRequest(
            @NotNull @Positive Long orderId,
            @NotBlank @Size(max = 255) String pspTransactionId,
            @NotNull PaymentStatus status,
            @NotNull @PositiveOrZero BigDecimal paidAmount,
            @NotBlank @Size(min = 3, max = 3) String currency
    ) {
    }

    public record PaymentCallbackResponse(Long orderId, ParkingOrder.Status status, SessionResponse session) {
    }

    public record SessionResponse(
            Long id,
            Long userId,
            Long zoneId,
            Long vehicleId,
            Instant startsAt,
            Instant expiresAt,
            BigDecimal paidAmount,
            String currency,
            String pspTransactionId
    ) {
    }
}
