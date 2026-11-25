package rs.ac.uns.ftn.sep.parking.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import rs.ac.uns.ftn.sep.parking.auth.ParkingUser;
import rs.ac.uns.ftn.sep.parking.vehicle.Vehicle;
import rs.ac.uns.ftn.sep.parking.zone.ParkingZone;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "parking_sessions", uniqueConstraints = @UniqueConstraint(
        name = "uk_parking_sessions_order", columnNames = "order_id"))
public class ParkingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private ParkingOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private ParkingUser owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false, updatable = false)
    private ParkingZone zone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false, updatable = false)
    private Vehicle vehicle;

    @Column(name = "starts_at", nullable = false, updatable = false)
    private Instant startsAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "paid_amount", nullable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;

    @Column(name = "psp_transaction_id", nullable = false, updatable = false, length = 255)
    private String pspTransactionId;

    protected ParkingSession() {
    }

    ParkingSession(ParkingOrder order, Instant startsAt) {
        this.order = order;
        this.owner = order.getOwner();
        this.zone = order.getZone();
        this.vehicle = order.getVehicle();
        this.startsAt = startsAt;
        this.expiresAt = startsAt.plus(order.getEffectiveDurationMinutes(), ChronoUnit.MINUTES);
        this.paidAmount = order.getTotalPrice();
        this.currency = order.getCurrency();
        this.pspTransactionId = order.getPspTransactionId();
    }

    Long getId() {
        return id;
    }

    Instant getStartsAt() {
        return startsAt;
    }

    Instant getExpiresAt() {
        return expiresAt;
    }

    BigDecimal getPaidAmount() {
        return paidAmount;
    }

    String getCurrency() {
        return currency;
    }

    String getPspTransactionId() {
        return pspTransactionId;
    }
}
