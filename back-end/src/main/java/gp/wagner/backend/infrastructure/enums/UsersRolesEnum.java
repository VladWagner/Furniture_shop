package gp.wagner.backend.infrastructure.enums;

// Роли для задания в UserDetails
public enum UsersRolesEnum {

    // Задание ролей на латинице
    ADMIN("Admin"),
    EDITOR("Editor"),
    CUSTOMER("Customer");

    UsersRolesEnum(String roleName) {
        this.roleName = roleName;
    }

    private final String roleName;

    public String getRoleName() {return roleName;}

    public static UsersRolesEnum getRoleEnum(String operationName) {

        return switch (operationName.toLowerCase()) {
            case "администратор" -> ADMIN;
            case "редактор" -> EDITOR;
            default -> CUSTOMER;
        };

    }
}
