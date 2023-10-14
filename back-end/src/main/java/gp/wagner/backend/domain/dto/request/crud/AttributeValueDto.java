package gp.wagner.backend.domain.dto.request.crud;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.Date;

//Значение атрибута = значение характеристики варианта товара
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueDto {

    //Для редактирования
    @Nullable
    private Long id;

    @NonNull
    @Min(1)
    private Long attributeId;


    //Строковое значение
    @Nullable
    private String strValue = null;

    //Целочисленное значение
    @Nullable
    private Integer intValue = null;

    //Значение с плавающей запятой
    @Nullable
    private Float floatValue = null;

    //Значение double
    @Nullable
    private Double doubleValue = null;

    //Значение bool
    @Nullable
    private Boolean boolValue = null;

    //Значение даты
    @Nullable
    private Date dateValue = null;

}
