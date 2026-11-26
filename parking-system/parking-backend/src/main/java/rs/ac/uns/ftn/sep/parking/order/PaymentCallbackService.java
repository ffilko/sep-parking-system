package rs.ac.uns.ftn.sep.parking.order;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.sep.parking.order.PaymentCallbackController.PaymentCallbackRequest;
import rs.ac.uns.ftn.sep.parking.order.PaymentCallbackController.PaymentCallbackResponse;
import rs.ac.uns.ftn.sep.parking.order.PaymentCallbackController.PaymentStatus;
import rs.ac.uns.ftn.sep.parking.order.PaymentCallbackController.SessionResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PaymentCallbackService {

    private final ParkingOrderRepository orders;
    private final ParkingSessionRepository sessions;
    private final PersistenceUnitUtil persistence;

    public PaymentCallbackService(ParkingOrderRepository orders, ParkingSessionRepository sessions,
                                  EntityManagerFactory entityManagerFactory) {
        this.orders = orders;
        this.sessions = sessions;
        this.persistence = entityManagerFactory.getPersistenceUnitUtil();
    }

    @Transactional
    public PaymentCallbackResponse confirm(PaymentCallbackRequest request) {
        ParkingOrder order = orders.findForPaymentUpdate(request.orderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking order not found"));

        if (order.getTotalPrice().compareTo(request.paidAmount()) != 0 || !order.getCurrency().equals(request.currency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount or currency does not match the order");
        }

        ParkingOrder.Status result = request.status() == PaymentStatus.SUCCESS
                ? ParkingOrder.Status.PAID : ParkingOrder.Status.PAYMENT_FAILED;

        if (order.getStatus() != ParkingOrder.Status.PENDING_PAYMENT) {
            if (order.getStatus() != result || !request.pspTransactionId().equals(order.getPspTransactionId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conflicting result for a finalized order");
            }
            ParkingSession existing = result == ParkingOrder.Status.PAID
                    ? sessions.findByOrder_Id(order.getId()).orElseThrow()
                    : null;
            return response(order, existing);
        }

        order.recordPaymentResult(result, request.pspTransactionId());
        ParkingSession session = result == ParkingOrder.Status.PAID
                ? sessions.save(new ParkingSession(order, Instant.now().truncatedTo(ChronoUnit.MICROS)))
                : null;
        return response(order, session);
    }

    private PaymentCallbackResponse response(ParkingOrder order, ParkingSession session) {
        SessionResponse details = session == null ? null : new SessionResponse(
                session.getId(),
                (Long) persistence.getIdentifier(order.getOwner()),
                order.getZone().getId(),
                (Long) persistence.getIdentifier(order.getVehicle()),
                session.getStartsAt(), session.getExpiresAt(), session.getPaidAmount(),
                session.getCurrency(), session.getPspTransactionId());
        return new PaymentCallbackResponse(order.getId(), order.getStatus(), details);
    }
}
