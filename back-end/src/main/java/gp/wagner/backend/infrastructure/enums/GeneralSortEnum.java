package gp.wagner.backend.infrastructure.enums;

//Тип сортировки по цене
public enum GeneralSortEnum {

    ASC("asc"),
    DESC("desc");

    GeneralSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
    public static GeneralSortEnum getSortType(String sort) {

        return switch (sort.toLowerCase()) {
            case "desc" -> DESC;
            default -> ASC;
        };

    }
}
