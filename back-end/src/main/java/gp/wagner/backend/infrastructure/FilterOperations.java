package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.entites.products.Product;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public enum FilterOperations {

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

