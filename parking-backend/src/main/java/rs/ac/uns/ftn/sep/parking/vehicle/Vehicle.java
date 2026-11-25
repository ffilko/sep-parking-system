package rs.ac.uns.ftn.sep.parking.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import rs.ac.uns.ftn.sep.parking.auth.ParkingUser;

@Entity
@Table(name = "vehicles", uniqueConstraints = @UniqueConstraint(
        name = "uk_vehicles_owner_registration_plate", columnNames = {"owner_id", "registration_plate"}))
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_plate", nullable = false, length = 20)
    private String registrationPlate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private ParkingUser owner;

    protected Vehicle() {
    }

    Vehicle(String registrationPlate, ParkingUser owner) {
        this.registrationPlate = registrationPlate;
        this.owner = owner;
    }

    Long getId() {
        return id;
    }

    String getRegistrationPlate() {
        return registrationPlate;
    }
}
