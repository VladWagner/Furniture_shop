package gp.wagner.backend.infrastructure.enums.sorting;

// Сортировка отзывов
public enum ReviewsSortEnum {

    ID("id"),
    VERIFIED("is_verified"),
    PRODUCT("product_id"),
    USER("user_id"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");

    ReviewsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static ReviewsSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "verified" -> VERIFIED;
            case "product" -> PRODUCT;
            case "user" -> USER;
            case "created" -> CREATED_AT;
            case "updated" -> UPDATED_AT;
            default -> ID;
        };

    }
}
