package gp.wagner.backend.domain.entities.tokens;

import gp.wagner.backend.domain.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;

// Токен для подтверждения почты
@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Пользователь. Связь один к одному, но без связующего свойства и у User
    // Каскадная связь вызывала проблемы с записью User - при удалении токена она так же удалялась
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER/*, cascade = CascadeType.ALL*/)
    @JoinColumn(name = "user_id")
    private User user;

    // Дата истечения срока действия токена
    @Column(name = "expiry_date")
    private Date expiryDate;

    // Токен
    @Column(name = "token")
    private String token;

    public VerificationToken(final String token, final User user) {

        this.id = null;
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate();
    }

    private Date calculateExpiryDate() {

        int expiryTimeInMinutes = 60 * 24;

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



