package gp.wagner.backend.infrastructure.enums.sorting;

//Тип сортировки товаров
public enum ViewsFrequencySortEnum {

    CATEGORY_ID("category_id"),
    CATEGORY_NAME("category_name"),
    VIEWS_COUNT("views"),
    VISITOR_COUNT("visits"),
    FREQUENCY("frequency");

    ViewsFrequencySortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static ViewsFrequencySortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "category_id" -> CATEGORY_ID;
            case "category_name" -> CATEGORY_NAME;
            case "views" -> VIEWS_COUNT;
            case "visits" -> VISITOR_COUNT;
            default -> FREQUENCY;
        };

    }
}
