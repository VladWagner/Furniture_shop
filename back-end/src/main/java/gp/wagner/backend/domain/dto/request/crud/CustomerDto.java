package gp.wagner.backend.domain.dto.request.crud;

import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.infrastructure.Utils;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.text.ParseException;
import java.util.Date;

//Объект для добавления/редактирования покупателя
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    //Для редактирования клиента
    @Nullable
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String surname;

    private String patronymic;

    @Nullable
    private String email;

    private long phoneNumber;

    //Создать объект сущности
    public Customer createEntity(){
        return new Customer();
    }


}

