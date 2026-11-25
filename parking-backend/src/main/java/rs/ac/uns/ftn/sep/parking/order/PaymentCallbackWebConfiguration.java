package rs.ac.uns.ftn.sep.parking.order;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Configuration
public class PaymentCallbackWebConfiguration implements WebMvcConfigurer {

    private final byte[] callbackSecret;

    public PaymentCallbackWebConfiguration(
            @Value("${parking.psp.callback-secret:${PARKING_PSP_CALLBACK_SECRET:}}") String configuredSecret) {
        callbackSecret = configuredSecret.isBlank() ? null : configuredSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if ("POST".equals(request.getMethod())) {
                    String provided = request.getHeader("X-PSP-Callback-Key");
                    if (callbackSecret == null || provided == null
                            || !MessageDigest.isEqual(callbackSecret, provided.getBytes(StandardCharsets.UTF_8))) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid PSP callback key");
                    }
                }
                return true;
            }
        }).addPathPatterns(PaymentCallbackController.PATH);
    }
}
