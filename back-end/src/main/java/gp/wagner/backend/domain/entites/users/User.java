package gp.wagner.backend.domain.entites.users;

import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.visits.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

//Пользователь
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Cacheable(value = false)
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Логин пользователя
    @Column(name = "login")
    private String userLogin;

    // Имя пользователя
    @Column(name = "name")
    private String name;

    // Email пользователя
    @Column(name = "email")
    private String email;

    // Флаг подтверждения почты
    @Column(name = "is_confirmed")
    private Boolean isConfirmed;

    // Изображение аккаунта пользователя
    @Column(name = "profile_img")
    private String profilePhoto;

    // Роль пользователя (Многие пользователи к 1 роли)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private UserRole userRole;

    // Связующее свойство для получения пароля пользователя
    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
    private UserPassword userPassword;

    // Дата регистрации
    @Column(name = "created_at")
    @CreatedDate
    private Instant createdAt;

    // Дата изменения пользователя
    @Column(name = "updated_at")
    @LastModifiedDate
    private Instant updatedAt;

    // Связующее свойство покупателя, если пользователь совершал заказы
    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
    private Customer customer;

    public User(Long id, String userLogin, String name, String email, UserRole userRole) {
        this.id = id;
        this.userLogin = userLogin;
        this.name = name;
        this.email = email;
        this.userRole = userRole;
        this.isConfirmed = false;
    }

    public boolean isEqualTo(User user){

        if(user == null)
            return false;

        return  Objects.equals(this.id, user.id) &&
                Objects.equals(this.userLogin, user.userLogin) &&
                Objects.equals(this.name, user.name) &&
                Objects.equals(this.email, user.email) &&
                Objects.equals(this.userRole.getId(), user.userRole.getId()) &&
                Objects.equals(this.isConfirmed, user.isConfirmed);
    }
}