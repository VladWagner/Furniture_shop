package gp.wagner.backend.domain.exceptions.suppliers;

import gp.wagner.backend.domain.exceptions.classes.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.function.Supplier;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeNotFound implements Supplier<ApiException> {

    // id
    private Long attributeId;

    // Список id по которым не был найден атрибут
    private List<Long> idsList;

    // Название атрибута по которому мог происходить поиск
    private String name;

    public ProductAttributeNotFound(Long attributeId) {
        this.attributeId = attributeId;
    }

    public ProductAttributeNotFound(List<Long> idsList) {
        this.idsList = idsList;
    }

    public ProductAttributeNotFound(String email) {
        this.name = email;
    }

    @Override
    public ApiException get() {

        // По флагу будет определяться, выводится ли сообщение для нескольких или только одного атрибута
        boolean listIsIncorrect = idsList == null || idsList.isEmpty();

        StringBuilder sb = listIsIncorrect ? new StringBuilder("Атрибут товара ") : new StringBuilder("Атрибуты товаров ");

        if (listIsIncorrect) {
            boolean attributeIdNotNull = attributeId != null;

            if (attributeIdNotNull)
                sb.append(String.format("с id %d ", attributeId));

            // Если задан и id и название атрибута
            if (name != null)
                sb.append(String.format("%s именем %s ", attributeIdNotNull ? "и" : "с", name));
        }
        else
            sb.append(String.format("со списком id [%d; %d] ", idsList.get(0), idsList.get(idsList.size()-1)));



        return new ApiException(sb.append(String.format("не %s!", listIsIncorrect ? "найден" : "найдены")).toString());
    }
}
