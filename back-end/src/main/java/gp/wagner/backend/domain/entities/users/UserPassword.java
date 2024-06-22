package gp.wagner.backend.domain.entities.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

//Пароль пользователя
@Entity
@Table(name = "users_passwords")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
