package gp.wagner.backend.domain.dto.response.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.users.UserRole;
import gp.wagner.backend.domain.entites.visits.Visitor;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;

//Объект передачи и вывода товара в списке товаров в виде карточки
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
// @JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRespDto {

    private Long id;

    @JsonProperty("user_login")
    private String userLogin;

    // Имя пользователя
    private String name = "";

    private String email;

    // Флаг подтверждения почты
    @JsonProperty("is_confirmed")
    private Boolean isConfirmed;

    // Аватарка пользователя
    @JsonProperty("profile_photo")
    private String profilePhoto;

    private RoleRespDto role;

    private Instant createdAt = null;

    // Дата изменения
    private Instant updatedAt = null;

    public UserRespDto(User user) {
        this.id = user.getId();
        this.userLogin = user.getUserLogin();
        this.name = user.getName() != null ? user.getName() : "";
        this.email = user.getEmail();
        this.isConfirmed = user.getIsConfirmed();
        this.profilePhoto = user.getProfilePhoto();
        this.role = new RoleRespDto(user.getUserRole());
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}

