package gp.wagner.backend.infrastructure;

//Тип сортировки по цене
public enum ProductsSortEnum {

    PRICE_ASC("price_asc"),
    PRICE_DESC("price_desc");

    ProductsSortEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getSortType() {return sortType;}
}
