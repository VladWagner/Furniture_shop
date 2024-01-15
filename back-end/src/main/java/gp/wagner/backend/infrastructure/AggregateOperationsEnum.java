package gp.wagner.backend.infrastructure;

//Тип сортировки по цене
public enum AggregateOperationsEnum {

    SUM("sum"),
    AVG("average"),
    MAX("max"),
    MIN("min");

    AggregateOperationsEnum(String sortType) {
        this.sortType = sortType;
    }

    private final String sortType;

    public String getOperationType() {return sortType;}
    public static AggregateOperationsEnum getOperationsType(String operationName) {

        return switch (operationName.toLowerCase()) {
            case "avg" -> AVG;
            case "max" -> MAX;
            case "min" -> MIN;
            default -> SUM;
        };

    }
}
