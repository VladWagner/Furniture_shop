package gp.wagner.backend.infrastructure.enums.sorting;

//Тип сортировки товаров
public enum DiscountsSortEnum {

    ID("id"),
    PERCENTAGE("percentage"),
    STARTS_AT("starts"),
    ENDS_AT("ends"),
    IS_ACTIVE("active");

    DiscountsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static DiscountsSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "percentage" -> PERCENTAGE;
            case "starts" -> STARTS_AT;
            case "ends" -> ENDS_AT;
            case "active" -> IS_ACTIVE;
            default -> ID;
        };

    }
}
