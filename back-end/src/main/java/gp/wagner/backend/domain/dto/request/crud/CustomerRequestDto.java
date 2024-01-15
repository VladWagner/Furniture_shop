package gp.wagner.backend.domain.dto.request.crud;

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

    private long phoneNumber;


}

