package gp.wagner.backend.domain.entites.tokens;

import gp.wagner.backend.domain.entites.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

//
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private static final int EXPIRATION_MINUTES = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Дата истечения срока действия токена
    @Column(name = "expires_at")
    private Date expiresAt;

    // Токен
    @Column(name = "token")
    private String token;


    // Не истёк ли срок действия токена
    public boolean isExpired(){
        return this.expiresAt.getTime() <= new Date().getTime();
    }

}



