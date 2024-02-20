package gp.wagner.backend.domain.exceptions.suppliers;

import gp.wagner.backend.domain.exceptions.classes.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

// Данный класс нужен для уменьшения кол-ва кода при выборе исключения в нескольких местах
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParentlessCategoryAlreadyExists implements Supplier<ApiException> {

    // id категории
    private Long categoryId;

    // Название категории
    private String categoryName;


    public ParentlessCategoryAlreadyExists(Long categoryId) {
        this.categoryId = categoryId;
    }

    public ParentlessCategoryAlreadyExists(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public ApiException get() {

        StringBuilder sb = new StringBuilder("Категория ");

        boolean categoryNameNotNull = categoryName != null;

        if (categoryNameNotNull)
            sb.append(String.format("с названием '%s' ", categoryName));

        // Если задано и id и название категории, тогда добавить союз «и»
        if (categoryId != null)
            sb.append(String.format("%2$s id: %1$s ", categoryId, categoryNameNotNull ? "и" : "c"));


        return new ApiException(sb.append("у которой не задан родитель уже существует!").toString());
    }
}
