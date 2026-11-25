package rs.ac.uns.ftn.sep.parking.zone;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ParkingDurationExceptionHandler {

    @ExceptionHandler(ParkingPriceService.InvalidDurationException.class)
    ProblemDetail invalidDuration(ParkingPriceService.InvalidDurationException exception) {
        return exception.getBody();
    }
}
