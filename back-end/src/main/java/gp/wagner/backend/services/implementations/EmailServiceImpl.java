package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.Utils;
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

    @Override
    public void sendOrderDetailsMime(Order createdOrder) throws MessagingException {
        MimeMessage passwordResetMail = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(passwordResetMail);
        helper.setFrom(env.getProperty("spring.mail.mail_from"));
        helper.setTo(createdOrder.getCustomer().getEmail());
        helper.setSubject(String.format("Заказ #%d был размещён успешно!", createdOrder.getCode()));
        String content = String.format("""
                <p>Здравствуйте %s, вы сформировали заказ в нашем магазине!</p>
                <p>Количество заказанных товаров: <b>%d</b></p>
                <p>Дата заказа: <b>%s</b></p>
                <p>Статус заказа: <b>%s</b></p>
                %s
                """, createdOrder.getCustomer().getName(),
                createdOrder.getGeneralProductsAmount(),
                Utils.sdf.format(createdOrder.getOrderDate()),
                createdOrder.getOrderState().getState(),
                Utils.opvTableView(createdOrder));
        helper.setText(content, true);

        mailSender.send(passwordResetMail);
    }

    @Override
    public void sendOrderCancelNotification(Order order) throws MessagingException {
        MimeMessage orderCancelMail = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(orderCancelMail);
        helper.setFrom(env.getProperty("spring.mail.mail_from"));
        helper.setTo(order.getCustomer().getEmail());
        helper.setSubject(String.format("Отмена заказа #%d", order.getCode()));
        String content = String.format("""
                <p>Здравствуйте %s!</p>
                <p>Заказ №%d от %5$s был отменён.</p>
                <p>Количество заказанных товаров: <b>%d</b></p>
                <p>Сумма заказа: <b>%s</b></p>
                """, order.getCustomer().getName(),
                order.getCode(),
                order.getGeneralProductsAmount(),
                Utils.intFormatter.format(order.getSum()),
                Utils.sdf.format(order.getOrderDate()));
        helper.setText(content, true);

        mailSender.send(orderCancelMail);
    }
}