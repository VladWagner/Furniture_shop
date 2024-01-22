package gp.wagner.backend.domain.dto.request.admin_panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.validation.orders_couting_filters_dto.annotations.ValidOrdersCountingFiltersRequestDto;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.Date;

//Значение атрибута = значение характеристики варианта товара
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidOrdersCountingFiltersRequestDto
public class OrdersAndBasketsCountFiltersRequestDto {

    //Нижняя граница диапазона
    @Nullable
    @JsonProperty("min_date")
    private Date minDate;

    //Верхняя граница диапазона
    @Nullable
    @JsonProperty("max_date")
    private Date maxDate;

    // Статус заказа
    @Nullable
    @JsonProperty("state_id")
    Integer stateId;

    // Категория заказанных товаров
    @Nullable
    @JsonProperty("category_id")
    Integer categoryId;

    // Нижняя граница стоимости варианта
    @Nullable
    @JsonProperty("price_min")
    Integer priceMin;

    // Верхняя граница
    @Nullable
    @JsonProperty("price_max")
    Integer priceMax;

    public boolean areDatesCorrect(){
        return (minDate != null && maxDate != null) && minDate.before(maxDate);
    }

    public boolean arePricesCorrect(){
        return (priceMin != null && priceMax != null) && priceMin < priceMax;
    }

}
