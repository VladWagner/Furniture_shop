package gp.wagner.backend.domain.entities.orders;

import gp.wagner.backend.domain.dto.request.crud.CustomerRequestDto;
import gp.wagner.backend.domain.entities.users.User;
import gp.wagner.backend.domain.entities.visits.Visitor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.Objects;

//Покупатели
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Посетитель, перешедший в разряд покупателя

    //Фамилия
    @Column(name = "surname")
    private String surname;

    //Имя
    @Column(name = "name")
    private String name;

    //Отчество
    @Column(name = "patronymic")
    private String patronymic;

    //Email
    @Column(name = "email")
    private String email;

    //Номер телефона
    @Column(name = "phone_number")
    private long phoneNumber;

    // Посетитель, с которого пришел заказ. Тип отношения м к 1, поскольку 1 посетитель может совершить заказ в виде разных покупателей
    @ManyToOne
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    // Пользователь, если таковой имеется с текущим email. 1 к 1, поскольку не может быть одного и того же пользователя с разными email
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    /*@OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @BatchSize(size = 256)
    private List<Order> orders;*/

    // Дата регистрации
    @Column(name = "created_at")
    @CreatedDate
    private Instant createdAt;

    // Дата изменения пользователя
    @Column(name = "updated_at")
    @LastModifiedDate
    private Instant updatedAt;

    public Customer(CustomerRequestDto dto) {
        this.id = dto.getId();
        this.surname = dto.getSurname();
        this.name = dto.getName();
        this.patronymic = dto.getPatronymic();
        this.email = dto.getEmail();
        this.phoneNumber = dto.getPhoneNumber();
    }
    public Customer(CustomerRequestDto dto, Visitor visitor) {
        this.id = dto.getId();
        this.surname = dto.getSurname();
        this.name = dto.getName();
        this.patronymic = dto.getPatronymic();
        this.email = dto.getEmail();
        this.phoneNumber = dto.getPhoneNumber();
        this.visitor = visitor;
    }

    public Customer(CustomerRequestDto dto, Visitor visitor, User user) {
        this(dto, visitor);

        this.user = user;
    }

    // Являются ли изменения в объекте необходимыми для его перезаписи в БД
    public boolean isEqualTo(Customer customer){

        if(customer == null)
            return false;

        return phoneNumber == customer.phoneNumber &&
                this.visitor.isEqualTo(customer.getVisitor()) &&
                this.user != null && this.user.isEqualTo(customer.user) &&
                Objects.equals(this.id, customer.id) &&
                Objects.equals(this.surname, customer.surname) &&
                Objects.equals(this.name, customer.name) &&
                Objects.equals(this.patronymic, customer.patronymic) &&
                Objects.equals(this.email, customer.email);

    }

}

