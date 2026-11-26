package rs.ac.uns.ftn.sep.parking.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingUserRepository extends JpaRepository<ParkingUser, Long> {

    Optional<ParkingUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
