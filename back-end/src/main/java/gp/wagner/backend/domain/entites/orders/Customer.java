package gp.wagner.backend.domain.entites.orders;

import gp.wagner.backend.domain.dto.request.crud.CustomerDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

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
    private long phoneNumber ;

    // Флаг подтверждения электронной почты
    @Column(name = "confirmed_email")
    private boolean confirmedEmail;

    public Customer(CustomerDto dto) {
        this.id = dto.getId();
        this.surname = dto.getSurname();
        this.name = dto.getName();
        this.patronymic = dto.getPatronymic();
        this.email = dto.getEmail();
        this.phoneNumber = dto.getPhoneNumber();
    }

    //Являются ли изменения в объекте необходимыми для его перезаписи в БД
    public boolean isEqualTo(Customer customer){

        if(customer == null)
            return false;

        // Текущий email не пустой и задаваемый не пустой, либо текущий пусто, а задаваемый - нет. В противном случае это не замена, а удаление
        //boolean replacingEmail = (!this.email.isEmpty() && !customer.email.isEmpty()) || (this.email.isEmpty() && !customer.email.isEmpty());
        //boolean replacingPhone = (!(this.phoneNumber <= 0) && !(customer.phoneNumber <= 0) || (this.phoneNumber <= 0 && !(customer.phoneNumber <= 0));


        return phoneNumber == customer.phoneNumber &&
                Objects.equals(this.id, customer.id) &&
                Objects.equals(this.surname, customer.surname) &&
                Objects.equals(this.name, customer.name) &&
                Objects.equals(this.patronymic, customer.patronymic) &&
                Objects.equals(this.email, customer.email);

    }

}

