package rs.ac.uns.ftn.sep.parking.zone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "parking_zones")
public class ParkingZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "min_duration_minutes", nullable = false)
    private int minDurationMinutes;

    @Column(name = "max_duration_minutes", nullable = false)
    private int maxDurationMinutes;

    protected ParkingZone() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public String getCurrency() {
        return currency;
    }

    public int getMinDurationMinutes() {
        return minDurationMinutes;
    }

    public int getMaxDurationMinutes() {
        return maxDurationMinutes;
    }
}
