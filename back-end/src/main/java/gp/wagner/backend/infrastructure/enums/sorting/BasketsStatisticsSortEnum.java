package gp.wagner.backend.infrastructure.enums.sorting;

// Тип сортировки статистики
public enum BasketsStatisticsSortEnum {

    DATE("added_date"),
    SUM("sum"),
    AMOUNT("amount"),
    VISITS("visits"),
    CVR("cvr");

    BasketsStatisticsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static BasketsStatisticsSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "sum" -> SUM;
            case "amount" -> AMOUNT;
            case "visits" -> VISITS;
            case "cvr" -> CVR;
            default -> DATE;
        };

    }
}
