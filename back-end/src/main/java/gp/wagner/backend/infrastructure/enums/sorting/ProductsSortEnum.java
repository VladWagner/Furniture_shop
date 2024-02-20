package gp.wagner.backend.infrastructure.enums.sorting;

//Тип сортировки товаров
public enum ProductsSortEnum {

    ID("id"),
    AVAILABLE("available"),

    // Позже сюда будет добавлена сортировка по размеру скидки

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
            default -> PRICE;
        };

    }
}
