package gp.wagner.backend.infrastructure.enums.sorting;

//Тип сортировки товаров
public enum ProductsOrVariantsCountSortEnum {

    PRODUCT_ID("p_id"),
    PV_ID("pv_id"),
    NAME("name"),
    PV_TITLE("title"),
    COUNT("count");

    ProductsOrVariantsCountSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static ProductsOrVariantsCountSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "p_id" -> PRODUCT_ID;
            case "pv_id" -> PV_ID;
            case "name" -> NAME;
            case "title" -> PV_TITLE;
            default -> COUNT;
        };

    }
}
