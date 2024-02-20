package gp.wagner.backend.infrastructure.enums.sorting;

//Тип сортировки просмотров категорий и товаров
public enum VisitorAndViewsSortEnum {

    ID("id"),
    VIEWS_AMOUNT("amount"),

    SUM("sum");

    VisitorAndViewsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static VisitorAndViewsSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "id" -> ID;
            case "amount" -> VIEWS_AMOUNT;
            default -> SUM;
        };

    }
}
