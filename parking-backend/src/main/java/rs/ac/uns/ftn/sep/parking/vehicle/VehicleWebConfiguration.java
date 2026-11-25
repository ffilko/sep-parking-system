package rs.ac.uns.ftn.sep.parking.vehicle;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import rs.ac.uns.ftn.sep.parking.auth.JwtService;
import rs.ac.uns.ftn.sep.parking.auth.ParkingUser;
import rs.ac.uns.ftn.sep.parking.auth.ParkingUserRepository;

@Configuration
public class VehicleWebConfiguration implements WebMvcConfigurer {

    static final String OWNER_ATTRIBUTE = "vehicleOwner";

    private final JwtService jwtService;
    private final ParkingUserRepository users;

    public VehicleWebConfiguration(JwtService jwtService, ParkingUserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if ("OPTIONS".equals(request.getMethod())) {
                    return true;
                }
                try {
                    long userId = jwtService.requireUserId(request.getHeader(HttpHeaders.AUTHORIZATION));
                    ParkingUser owner = users.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                    request.setAttribute(OWNER_ATTRIBUTE, new AuthenticatedOwner(userId, owner));
                    return true;
                } catch (ResponseStatusException exception) {
                    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                    throw exception;
                }
            }
        }).addPathPatterns("/api/vehicles");
    }

    record AuthenticatedOwner(long id, ParkingUser user) {
    }
}
