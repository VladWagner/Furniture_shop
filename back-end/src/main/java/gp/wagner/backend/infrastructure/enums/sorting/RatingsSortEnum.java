package gp.wagner.backend.infrastructure.enums.sorting;

// Сортировка оценок
public enum RatingsSortEnum {

    ID("id"),
    RATING("rating"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");

    RatingsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static RatingsSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "rating" -> RATING;
            case "created" -> CREATED_AT;
            case "updated" -> UPDATED_AT;
            default -> ID;
        };

    }
}
