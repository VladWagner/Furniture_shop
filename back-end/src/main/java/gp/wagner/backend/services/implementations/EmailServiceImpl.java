package gp.wagner.backend.services.implementations;

import gp.wagner.backend.services.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    // Использование переменных окружения
    @Autowired
    private Environment env;

    private JavaMailSender mailSender;

    @Autowired
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetTokenSimple(String email, String token, String userLogin) {

        SimpleMailMessage passwordResetMail = new SimpleMailMessage();

        passwordResetMail.setFrom(env.getProperty("spring.mail.mail_from"));
        passwordResetMail.setTo(email);
        passwordResetMail.setSubject(String.format("Сброс пароля для пользователя %s", userLogin));
        passwordResetMail.setText(String.format("""
                \n Для обновления пароля скопируйте или кликните по ссылке (кнопке) ниже:
                \n\t http://localhost:8080/reset?token=%1$s.
                \n\t Токен: %1$s
                \n Данная ссылка действительна только 24ч с момента отправки!
                """, token));

        mailSender.send(passwordResetMail);
    }

    @Override
    public void sendPasswordResetTokenMime(String email, String token, String userLogin) throws MessagingException {
        MimeMessage passwordResetMail = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(passwordResetMail);
        helper.setFrom(env.getProperty("spring.mail.mail_from"));
        helper.setTo(email);
        helper.setSubject(String.format("Сброс пароля для пользователя %s", userLogin));
        String content = String.format("""
                <p>Для сброса пароля нажмите на ссылку ниже:</p>
                <a href='http://localhost:8080/reset?token=%1$s'>Сбросить пароль</a>
                <p>Токен: <b>%1$s</b></p>
                """, token);
        helper.setText(content, true);

        mailSender.send(passwordResetMail);
    }

    @Override
    public void sendConfirmationTokenMime(String email, String token, String userLogin) throws MessagingException {
        MimeMessage passwordResetMail = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(passwordResetMail);
        helper.setFrom(env.getProperty("spring.mail.mail_from"));
        helper.setTo(email);
        helper.setSubject(String.format("Подтверждение почты для пользователя %s", userLogin));
        String content = String.format("""
                <p>Для подтверждение перейдите по ссылке ниже ↓:</p>
                <a href='http://localhost:8080/confirm?token=%1$s'>Подтвердить аккаунт</a>
                <p>Токен: <b>%1$s</b></p>
                """, token);
        helper.setText(content, true);

        mailSender.send(passwordResetMail);
    }
}
