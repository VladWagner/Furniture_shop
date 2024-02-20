package gp.wagner.backend.services.interfaces;


import jakarta.mail.MessagingException;

public interface EmailService {

    void sendPasswordResetTokenSimple(String email, String token, String userLogin);
    void sendPasswordResetTokenMime(String email, String token, String userLogin) throws MessagingException;
    void sendConfirmationTokenMime(String email, String token, String userLogin) throws MessagingException;


}
