package gp.wagner.backend.domain.dto.response.filters;


import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.dto.response.users.RoleRespDto;
import gp.wagner.backend.domain.entites.users.UserRole;
import jakarta.annotation.Nullable;
import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//DTO для формирования значений бокового фильтра
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserFilterValuesDto {

    // Существующие роли пользователей
    @Nullable
    private List<RoleRespDto> roles;

    // Минимальная дата регистрации
    @Nullable
    @JsonProperty("lo_registration_date")
    private Date minDate;

    // Максимальная дата регистрации
    @Nullable
    @JsonProperty("hi_registration_date")
    private Date maxDate;

    // ctor с mapping'ом коллекции ролей
    public UserFilterValuesDto(Date minDate, Date maxDate, List<UserRole> roles) {
        this.roles = roles.stream()
                .map(RoleRespDto::new)
                .collect(Collectors.toList());
        this.minDate = minDate;
        this.maxDate = maxDate;
    }
}
