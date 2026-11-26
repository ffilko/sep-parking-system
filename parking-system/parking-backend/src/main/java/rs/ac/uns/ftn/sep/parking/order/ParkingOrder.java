package rs.ac.uns.ftn.sep.parking.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import rs.ac.uns.ftn.sep.parking.auth.ParkingUser;
import rs.ac.uns.ftn.sep.parking.vehicle.Vehicle;
import rs.ac.uns.ftn.sep.parking.zone.ParkingPriceResponse;
import rs.ac.uns.ftn.sep.parking.zone.ParkingZone;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "parking_orders")
public class ParkingOrder {

    public enum Status {
        PENDING_PAYMENT,
        PAID,
        PAYMENT_FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private ParkingUser owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false, updatable = false)
    private ParkingZone zone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false, updatable = false)
    private Vehicle vehicle;

    @Column(name = "duration_minutes", nullable = false, updatable = false)
    private int durationMinutes;

    @Column(name = "effective_duration_minutes", nullable = false, updatable = false)
    private long effectiveDurationMinutes;

    @Column(name = "billable_hours", nullable = false, updatable = false)
    private int billableHours;

    @Column(name = "hourly_rate", nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "total_price", nullable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status;

    @Column(name = "psp_transaction_id", length = 255)
    private String pspTransactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ParkingOrder() {
    }

    ParkingOrder(ParkingUser owner, ParkingZone zone, Vehicle vehicle, ParkingPriceResponse price) {
        this.owner = owner;
        this.zone = zone;
        this.vehicle = vehicle;
        this.durationMinutes = price.durationMinutes();
        this.effectiveDurationMinutes = price.effectiveDurationMinutes();
        this.billableHours = price.billableHours();
        this.hourlyRate = zone.getHourlyRate();
        this.totalPrice = price.totalPrice();
        this.currency = price.currency();
        this.status = Status.PENDING_PAYMENT;
        this.createdAt = Instant.now();
    }

    Long getId() {
        return id;
    }

    BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    BigDecimal getTotalPrice() {
        return totalPrice;
    }

    String getCurrency() {
        return currency;
    }

    Status getStatus() {
        return status;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    ParkingUser getOwner() {
        return owner;
    }

    ParkingZone getZone() {
        return zone;
    }

    Vehicle getVehicle() {
        return vehicle;
    }

    long getEffectiveDurationMinutes() {
        return effectiveDurationMinutes;
    }

    String getPspTransactionId() {
        return pspTransactionId;
    }

    void recordPaymentResult(Status result, String transactionId) {
        status = result;
        pspTransactionId = transactionId;
    }
}
