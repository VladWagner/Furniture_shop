package gp.wagner.backend.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilterOperationsEnum {

    EQUALS("=="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    BETWEEN("~"),

    //Операции у блоков фильтрации
    OR("or"),
    AND("and");

    private final String value;

}

