package gp.wagner.backend.infrastructure.enums.sorting.orders;

// Тип сортировки статистики
public enum OrdersStatisticsSortEnum {

    DATE("date"),
    SUM("sum"),
    AMOUNT("amount"),
    VISITS("visits"),
    CVR("cvr");

    OrdersStatisticsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static OrdersStatisticsSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "sum" -> SUM;
            case "amount" -> AMOUNT;
            case "visits" -> VISITS;
            case "cvr" -> CVR;
            default -> DATE;
        };

    }
}
