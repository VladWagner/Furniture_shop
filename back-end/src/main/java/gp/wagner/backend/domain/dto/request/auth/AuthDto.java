package gp.wagner.backend.domain.dto.request.auth;

import gp.wagner.backend.validation.user_request_dto.annotations.ValidUserRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Объект для добавления/редактирования пользователя
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidUserRequestDto
public class AuthDto {

    @NotNull
    private String login;

    @NotNull
    private String password;


}

