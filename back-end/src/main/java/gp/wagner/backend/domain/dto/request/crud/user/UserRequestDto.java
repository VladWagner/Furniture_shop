package gp.wagner.backend.domain.dto.request.crud.user;

import gp.wagner.backend.validation.user_request_dto.annotations.ValidUserRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
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
public class UserRequestDto {

    //Для редактирования пользователя
    @Nullable
    private Long id;

    @Nullable
    private String name;

    @NotNull
    private String login;

    @NotNull
    private String email;

    // Если происходит редактирование пользователя, тогда пароль можно не задавать
    @Nullable
    private String password;


}

