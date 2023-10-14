package gp.wagner.backend.domain.entites.users;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

//Роль пользователя
@Entity
@Table(name = "user_roles")
@Getter
@Setter
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Роль
    @Column(name = "role")
    private String role;
}
