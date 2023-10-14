package gp.wagner.backend.domain.entites.users;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

//Пароль пользователя
@Entity
@Table(name = "users_passwords")
@Getter
@Setter
public class UserPassword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Пароль пользователя
    @Column(name = "password")
    private String password;

    //Связующее свойство пользователя
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
