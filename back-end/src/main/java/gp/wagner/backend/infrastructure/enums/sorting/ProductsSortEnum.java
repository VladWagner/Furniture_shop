package gp.wagner.backend.infrastructure.enums.sorting;

//Тип сортировки товаров
public enum ProductsSortEnum {

    ID("id"),
    AVAILABLE("available"),

    // Сортировка по размеру скидки
    DISCOUNT("discount"),

    // Количество оценок
    RATINGS_AMOUNT("ratings"),

    PRICE("price");

    ProductsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static ProductsSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "id" -> ID;
            case "available" -> AVAILABLE;
            case "discount" -> DISCOUNT;
            case "ratings" -> RATINGS_AMOUNT;
            default -> PRICE;
        };

    }
}
