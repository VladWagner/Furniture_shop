package gp.wagner.backend.infrastructure.enums.sorting;

// Сортировка покупателей
public enum CustomersSortEnum {

    ID("id"),
    SNP("snp"),
    EMAIL("email"),
    PHONE("phone"),
    CREATED("created"),
    ORDERS_COUNT("orders_count"),
    UNITS_COUNT("units_count"),
    AVG_UNIT_PRICE("avg_unit_price"),
    ORDERS_SUM("sum");

    CustomersSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static CustomersSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "snp" -> SNP;
            case "email" -> EMAIL;
            case "phone" -> PHONE;
            case "created" -> CREATED;
            case "orders_count" -> ORDERS_COUNT;
            case "units_count" -> UNITS_COUNT;
            case "avg_price" -> AVG_UNIT_PRICE;
            case "sum" -> ORDERS_SUM;
            default -> ID;
        };

    }
}
