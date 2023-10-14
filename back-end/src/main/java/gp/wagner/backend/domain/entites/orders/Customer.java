package gp.wagner.backend.domain.entites.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

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
    private int phoneNumber ;


}
