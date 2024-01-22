package gp.wagner.backend.infrastructure.enums;

//Тип сортировки по цене
public enum StatisticsObjectEnum {


    // Для конкретного запроса таблице заказов
    PRODUCTS("products"),
    VARIANTS("variants");

    StatisticsObjectEnum(String statisticsType) {
        this.statisticsType = statisticsType;
    }

    private final String statisticsType;

    public String getOperationType() {return statisticsType;}
}
