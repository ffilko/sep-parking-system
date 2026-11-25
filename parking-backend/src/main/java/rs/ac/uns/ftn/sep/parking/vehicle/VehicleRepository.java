package rs.ac.uns.ftn.sep.parking.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByOwner_IdAndRegistrationPlate(Long ownerId, String registrationPlate);

    List<Vehicle> findAllByOwner_IdOrderByRegistrationPlateAsc(Long ownerId);
}
