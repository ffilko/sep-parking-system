package rs.ac.uns.ftn.sep.parking.vehicle;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.sep.parking.auth.ParkingUser;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicles;

    public VehicleService(VehicleRepository vehicles) {
        this.vehicles = vehicles;
    }

    Vehicle addVehicle(Long ownerId, ParkingUser owner, String registrationPlate) {
        if (vehicles.existsByOwner_IdAndRegistrationPlate(ownerId, registrationPlate)) {
            throw duplicateVehicle();
        }
        try {
            return vehicles.saveAndFlush(new Vehicle(registrationPlate, owner));
        } catch (DataIntegrityViolationException exception) {
            if (vehicles.existsByOwner_IdAndRegistrationPlate(ownerId, registrationPlate)) {
                throw duplicateVehicle();
            }
            throw exception;
        }
    }

    List<Vehicle> listVehicles(Long ownerId) {
        return vehicles.findAllByOwner_IdOrderByRegistrationPlateAsc(ownerId);
    }

    private static ResponseStatusException duplicateVehicle() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Registration plate already registered for this user");
    }
}
