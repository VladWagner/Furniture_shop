package gp.wagner.backend.domain.dto.request.crud;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gp.wagner.backend.infrastructure.serializers.DateTimeJsonDeserializer;
import gp.wagner.backend.validation.discount_request_dto.annotations.ValidDiscountRequestDto;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.Date;
import java.util.List;

//Объект для добавления/редактирования скидки
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidDiscountRequestDto
public class DiscountRequestDto {

    @Nullable
    private Long id;

    // % скидки
    @Nullable
    private Float percentage;

    // Дата начала действия скидки
    @Nullable
    @JsonProperty("starts_at")
    @JsonDeserialize(using = DateTimeJsonDeserializer.class)
    private Date startsAt;

    // Дата окончания срока действия скидки
    @Nullable
    @JsonDeserialize(using = DateTimeJsonDeserializer.class)
    @JsonProperty("ends_at")
    private Date endsAt;

    // Флаг вывода активности скидки
    //@NotNull
    @Nullable
    @JsonProperty("is_active")
    private Boolean isActive;


    // Флаг бессрочности действия скидки
    //@NotNull
    @Nullable
    @JsonProperty("is_infinite")
    private Boolean isInfinite;

    // Id вариантов к которым нужно добавить скидку
    @Nullable
    @JsonProperty("products_variants_ids")
    private List<Long> productsVariantsIds;

    // Id товаров ко всем вариантам которых нужно добавить скидку
    @Nullable
    @JsonProperty("products_ids")
    private List<Long> productsIds;

    // Id категории к товарам которой нужно добавить скидку
    @Nullable
    @JsonProperty("category_id")
    private Long categoryId;

    // Id вариантов у которых нужно добавить скидку
    @Nullable
    @JsonProperty("removed_variants_ids")
    private List<Long> removedVariantsIds;

    // Id товаров у всех вариантам которых нужно добавить скидку
    @Nullable
    @JsonProperty("removed_products_ids")
    private List<Long> removedProductsIds;

    // Id категорий у которых нужно добавить скидку
    @Nullable
    @JsonProperty("removed_categories_ids")
    private List<Long> removedCategoriesIds;
}