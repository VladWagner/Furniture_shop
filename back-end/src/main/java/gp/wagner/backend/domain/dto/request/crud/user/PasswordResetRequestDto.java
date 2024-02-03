package gp.wagner.backend.domain.dto.request.crud.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Объект сброса пароля и задания нового
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {

    @NotNull
    @JsonProperty("new_password")
    private String newPassword;

    @NotNull
    private String token;

}

