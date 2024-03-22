package gp.wagner.backend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

// DTO для передачи access & refresh токена аутентифицированному пользователю
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtRespDto {

    @JsonProperty("header_type")
    private String headerType = "Bearer ";

    @NotNull
    @JsonProperty("access_token")
    private String accessToken;

    @NotNull
    @JsonProperty("refresh_token")
    private String refreshToken;

    public JwtRespDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
