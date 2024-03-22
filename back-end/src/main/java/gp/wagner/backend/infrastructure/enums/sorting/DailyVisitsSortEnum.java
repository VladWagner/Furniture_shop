package gp.wagner.backend.infrastructure.enums.sorting;

// Сортировка записей дневных посещений
public enum DailyVisitsSortEnum {

    COUNT("count"),
    DATE("date");

    DailyVisitsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static DailyVisitsSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "date" -> DATE;
            default -> COUNT;
        };

    }
}
