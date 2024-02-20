package gp.wagner.backend.infrastructure.enums.sorting;

//Тип сортировки просмотров категорий
public enum VisitsorAndProductsViewsSortEnum {

    ID("id"),
    PRODUCTS_AMOUNT("amount"),

    SUM("sum");

    VisitsorAndProductsViewsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static VisitsorAndProductsViewsSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "id" -> ID;
            case "amount" -> PRODUCTS_AMOUNT;
            default -> SUM;
        };

    }
}
