package gp.wagner.backend.infrastructure.enums;

// Тип фильтрации/статистической выборки
public enum ProductsOrVariantsEnum {

    // Для конкретного запроса таблице заказов
    PRODUCTS("products"),
    VARIANTS("variants");

    ProductsOrVariantsEnum(String statisticsType) {
        this.statisticsType = statisticsType;
    }

    private final String statisticsType;

    public String getOperationType() {return statisticsType;}
}
