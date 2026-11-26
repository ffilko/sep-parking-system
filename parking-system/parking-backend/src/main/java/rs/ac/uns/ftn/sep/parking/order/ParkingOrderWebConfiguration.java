package rs.ac.uns.ftn.sep.parking.order;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import rs.ac.uns.ftn.sep.parking.auth.JwtService;

@Configuration
public class ParkingOrderWebConfiguration implements WebMvcConfigurer {

    private final JwtService jwtService;

    public ParkingOrderWebConfiguration(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if ("POST".equals(request.getMethod())) {
                    try {
                        long ownerId = jwtService.requireUserId(request.getHeader(HttpHeaders.AUTHORIZATION));
                        request.setAttribute(ParkingOrderController.OWNER_ID_ATTRIBUTE, ownerId);
                    } catch (ResponseStatusException exception) {
                        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                        throw exception;
                    }
                }
                return true;
            }
        }).addPathPatterns(ParkingOrderController.PATH);
    }
}
