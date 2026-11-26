package rs.ac.uns.ftn.sep.parking.order;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.sep.parking.vehicle.Vehicle;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface ParkingOrderRepository extends JpaRepository<ParkingOrder, Long> {

    @Query("""
            select o.id as id, o.status as status, z.id as zoneId, z.name as zoneName,
                   v.id as vehicleId, v.registrationPlate as registrationPlate,
                   o.durationMinutes as durationMinutes, o.effectiveDurationMinutes as effectiveDurationMinutes,
                   o.billableHours as billableHours, o.totalPrice as totalPrice,
                   o.currency as currency, o.createdAt as createdAt,
                   s.id as sessionId, s.startsAt as startsAt, s.expiresAt as expiresAt
            from ParkingOrder o
            join o.zone z
            join o.vehicle v
            left join ParkingSession s on s.order = o
            where o.id = :orderId and o.owner.id = :ownerId
            """)
    Optional<OrderStatus> findStatusByIdAndOwner(@Param("orderId") Long orderId, @Param("ownerId") long ownerId);

    interface OrderStatus {
        Long getId();
        ParkingOrder.Status getStatus();
        Long getZoneId();
        String getZoneName();
        Long getVehicleId();
        String getRegistrationPlate();
        int getDurationMinutes();
        long getEffectiveDurationMinutes();
        int getBillableHours();
        BigDecimal getTotalPrice();
        String getCurrency();
        Instant getCreatedAt();
        Long getSessionId();
        Instant getStartsAt();
        Instant getExpiresAt();
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ParkingOrder o where o.id = :orderId")
    Optional<ParkingOrder> findForPaymentUpdate(@Param("orderId") Long orderId);

    @Query("select v from Vehicle v where v.id = :vehicleId and v.owner.id = :ownerId")
    Optional<Vehicle> findOwnedVehicle(@Param("vehicleId") Long vehicleId, @Param("ownerId") Long ownerId);
}
