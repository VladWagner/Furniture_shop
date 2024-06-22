package gp.wagner.backend.domain.entities.users;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

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

    // Пользователи с данной ролью
    @OneToMany(mappedBy = "userRole")
    @BatchSize(size = 256)
    private List<User> users = new ArrayList<>();
}
