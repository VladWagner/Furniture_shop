package gp.wagner.backend.domain.entities.tokens;

import gp.wagner.backend.domain.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;

//
@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    private static final int EXPIRATION_MINUTES = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Пользователь. Связь один к одному, но без связующего свойтсва и у User
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    // Дата истечения срока действия токена
    @Column(name = "expiry_date")
    private Date expiryDate;

    // Токен
    @Column(name = "token")
    private String token;

    public PasswordResetToken(final String token, final User user) {

        this.id = null;
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION_MINUTES);
    }


    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();

        // Получить текущую дату в миллисекундах
        cal.setTimeInMillis(new Date().getTime());

        // Задать смещение относительно текущей даты в минутах
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    // Не истёк ли срок действия токена
    public boolean isExpired(){
        return this.expiryDate.getTime() <= new Date().getTime();
    }

}



