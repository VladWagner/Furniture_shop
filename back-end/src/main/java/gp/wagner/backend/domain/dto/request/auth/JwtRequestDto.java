package gp.wagner.backend.domain.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.*;

// DTO для передачи access & refresh токена аутентифицированному пользователю
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequestDto {

    @Nullable
    @JsonProperty("access_token")
    private String accessToken;

    @Nullable
    @JsonProperty("refresh_token")
    private String refreshToken;

}
