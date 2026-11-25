package rs.ac.uns.ftn.sep.parking.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    Optional<ParkingSession> findByOrder_Id(Long orderId);

    @Query("""
            select s.id as id, z.id as zoneId, z.name as zoneName,
                   v.id as vehicleId, v.registrationPlate as registrationPlate,
                   s.startsAt as startsAt, s.expiresAt as expiresAt,
                   s.paidAmount as paidAmount, s.currency as currency
            from ParkingSession s
            join s.zone z
            join s.vehicle v
            where s.owner.id = :ownerId and s.expiresAt > :now
            order by s.expiresAt, s.id
            """)
    List<ActiveSession> findActiveByOwner(@Param("ownerId") long ownerId, @Param("now") Instant now);

    @Query("""
            select s.id as id, z.id as zoneId, z.name as zoneName,
                   v.id as vehicleId, v.registrationPlate as registrationPlate,
                   s.startsAt as startsAt, s.expiresAt as expiresAt,
                   s.paidAmount as paidAmount, s.currency as currency,
                   s.pspTransactionId as pspTransactionId
            from ParkingSession s
            join s.zone z
            join s.vehicle v
            where s.owner.id = :ownerId and s.expiresAt <= :now
            order by s.startsAt desc, s.id desc
            """)
    List<HistoricalSession> findHistoryByOwner(@Param("ownerId") long ownerId, @Param("now") Instant now);

    interface HistoricalSession extends ActiveSession {
        String getPspTransactionId();
    }

    interface ActiveSession {
        Long getId();
        Long getZoneId();
        String getZoneName();
        Long getVehicleId();
        String getRegistrationPlate();
        Instant getStartsAt();
        Instant getExpiresAt();
        BigDecimal getPaidAmount();
        String getCurrency();
    }
}
