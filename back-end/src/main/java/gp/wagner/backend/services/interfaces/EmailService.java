package gp.wagner.backend.services.interfaces;


import gp.wagner.backend.domain.entites.orders.Order;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;

public interface EmailService {

    void sendPasswordResetTokenSimple(String email, String token, String userLogin);
    void sendPasswordResetTokenMime(String email, String token, String userLogin) throws MessagingException;

    void sendConfirmationTokenMime(String email, String token, String userLogin) throws MessagingException;

    void sendOrderDetailsMime(Order createdOrder) throws MessagingException;

    @Async
    void sendOrderCancelNotification(Order orderToCancel) throws MessagingException;
}
