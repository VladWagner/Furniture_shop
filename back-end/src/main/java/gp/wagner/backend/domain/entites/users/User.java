package gp.wagner.backend.domain.entites.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

//Пользователь
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Логин пользователя
    @Column(name = "login")
    private String userLogin;

    //Email пользователя
    @Column(name = "email")
    private String email;

    //Роль пользователя (Многие пользователи к 1 роли)
    @ManyToOne
    @JoinColumn(name = "role_id")
    private UserRole userRole;

    //Связующее свойство для получения пароля пользователя
    @OneToOne(mappedBy = "user")
    private UserPassword userPassword;

}
