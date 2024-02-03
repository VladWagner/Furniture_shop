package gp.wagner.backend.domain.dto.request.crud.user;

import gp.wagner.backend.validation.user_request_dto.annotations.ValidUserRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Объект для обновление пароля пользователя
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateRequestDto {

    @NotNull
    private Long userId;

    @Nullable
    private String email;

    @NotNull
    private String oldPassword;

    @NotNull
    private String newPassword;

}

