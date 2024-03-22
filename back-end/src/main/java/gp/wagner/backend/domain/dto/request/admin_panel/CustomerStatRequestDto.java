package gp.wagner.backend.domain.dto.request.admin_panel;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

// DTO с данными покупателя для статистических выборок
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatRequestDto {

    //Для редактирования клиента
    @Nullable
    private Long id;

    @Nullable
    private String name;

    @Nullable
    private String surname;

    @Nullable
    private String patronymic;

    @Nullable
    private String email;

    @Nullable
    private long phoneNumber;


}

