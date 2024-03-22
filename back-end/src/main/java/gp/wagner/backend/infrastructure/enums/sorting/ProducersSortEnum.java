package gp.wagner.backend.infrastructure.enums.sorting;

//Тип сортировки производителей
public enum ProducersSortEnum {

    ID("id"),
    PRODUCER_NAME("name"),
    DELETED_AT("deleted"),
    IS_SHOWN("is_shown");

    ProducersSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static ProducersSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "name" -> PRODUCER_NAME;
            case "deleted" -> DELETED_AT;
            case "shown" -> IS_SHOWN;
            default -> ID;
        };

    }
}
