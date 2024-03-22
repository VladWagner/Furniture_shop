package gp.wagner.backend.domain.dto.request.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.sql.Date;

// Фильтр для выборки пользователей
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersFilterRequestDto {

    // Пользователь по определённому Id
    @Nullable
    private Long id;

    // Флаг показа
    @Nullable
    @JsonProperty("is_confirmed")
    private Boolean isConfirmed;

    // Роль пользователя
    @Nullable
    @JsonProperty("role_id")
    private Long role;

    // Флаг является ли пользователь покупателем
    @Nullable
    @JsonProperty("is_customer")
    private Boolean isCustomer;

    // Минимальная дата регистрации
    @Nullable
    @JsonProperty("date_lo")
    private Date minDate;

    // Максимальная дата регистрации
    @Nullable
    @JsonProperty("date_hi")
    private Date maxDate;

}
