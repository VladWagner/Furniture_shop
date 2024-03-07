package gp.wagner.backend.infrastructure.enums.sorting.orders;

//Тип сортировки товаров
public enum OrdersSortEnum {

    ID("id"),
    SUM("sum"),
    ORDER_STATE("order_state"),

    // Сортировка по способу оплаты
    PAYMENT_METHOD("payment_method"),

    // Количество товаров в заказе
    FULLNESS("fullness");

    OrdersSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static OrdersSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "id" -> ID;
            case "order_state" -> ORDER_STATE;
            case "payment" -> PAYMENT_METHOD;
            case "fullness" -> FULLNESS;
            default -> SUM;
        };

    }
}
