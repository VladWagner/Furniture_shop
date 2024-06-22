package gp.wagner.backend.domain.dto.response.users;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entities.users.UserRole;
import lombok.*;

//Объект для добавления производителя
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleRespDto {

    private Long id;

    // Название роли
    @JsonProperty("user_role")
    private String userRole;

    public RoleRespDto(UserRole userRole) {
        this.id = userRole.getId();
        this.userRole = userRole.getRole();
    }
}
