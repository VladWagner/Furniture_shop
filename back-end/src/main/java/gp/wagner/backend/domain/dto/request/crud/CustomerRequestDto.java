package gp.wagner.backend.domain.dto.request.crud;

import gp.wagner.backend.validation.customer_request_dto.annotations.ValidCustomerRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

//Объект для добавления/редактирования покупателя
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidCustomerRequestDto
public class CustomerRequestDto {

    //Для редактирования клиента
    @Nullable
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @Nullable
    private String surname;

    @Nullable
    private String patronymic;

    @Nullable
    private String email;

    @Nullable
    private Long phoneNumber;


}

