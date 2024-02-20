package gp.wagner.backend.domain.exceptions.suppliers;

import gp.wagner.backend.domain.exceptions.classes.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleNotFound implements Supplier<ApiException> {

    // id роли
    private Long roleId;

    // Название роли, по которой мог идти поиск
    private String roleName;

    public UserRoleNotFound(Long roleId) {
        this.roleId = roleId;
    }


    @Override
    public ApiException get() {

        StringBuilder sb = new StringBuilder("Роль ");

        boolean roleIdNotNull = roleId != null;

        if (roleIdNotNull)
            sb.append(String.format("с id %d ", roleId));

        // Если задано id и наименование роли пользователя
        if (roleName != null && !roleName.isEmpty())
            sb.append(String.format("%2$s названием %1$s ", roleName, roleIdNotNull ? "и" : "с"));


        return new ApiException(sb.append("не найдена!").toString());
    }
}
