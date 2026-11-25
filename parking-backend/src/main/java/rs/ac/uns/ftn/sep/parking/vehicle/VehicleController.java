package rs.ac.uns.ftn.sep.parking.vehicle;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse addVehicle(
            @RequestAttribute(VehicleWebConfiguration.OWNER_ATTRIBUTE) VehicleWebConfiguration.AuthenticatedOwner owner,
            @Valid @RequestBody VehicleRequest request) {
        return VehicleResponse.from(vehicleService.addVehicle(owner.id(), owner.user(), request.registrationPlate()));
    }

    @GetMapping
    public List<VehicleResponse> listVehicles(
            @RequestAttribute(VehicleWebConfiguration.OWNER_ATTRIBUTE) VehicleWebConfiguration.AuthenticatedOwner owner) {
        return vehicleService.listVehicles(owner.id()).stream().map(VehicleResponse::from).toList();
    }

    public record VehicleRequest(@NotBlank @Size(max = 20) String registrationPlate) {

        public VehicleRequest {
            if (registrationPlate != null) {
                registrationPlate = registrationPlate.strip().toUpperCase(Locale.ROOT);
            }
        }
    }

    public record VehicleResponse(Long id, String registrationPlate) {

        static VehicleResponse from(Vehicle vehicle) {
            return new VehicleResponse(vehicle.getId(), vehicle.getRegistrationPlate());
        }
    }
}
