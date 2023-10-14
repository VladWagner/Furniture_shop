package gp.wagner.backend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import gp.wagner.backend.domain.entites.eav.AttributeValue;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//DTO для передачи значений атрибутов (характеристик) конкретного товара
@Data
@NoArgsConstructor
@AllArgsConstructor
//Попытка борьбы со связями между сущностями
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeValueRespDto {

    //Id объекта сущности со значением под конкретный товар
    @Nullable
    private Long attributeValueId;
    private String attributeName;
    private Long attributeId;


    //Строковое значение
    private String strValue = null;

    //Целочисленное значение
    private Integer intValue = null;

    //Значение с плавающей запятой
    private Float floatValue = null;

    //Значение double
    private Double doubleValue = null;

    //Значение bool
    private Boolean boolValue = null;

    //Значение даты
    private Date dateValue = null;

    public AttributeValueRespDto(AttributeValue av) {
        this.attributeValueId = av.getId();
        this.attributeName = av.getAttribute().getAttributeName();
        this.attributeId = av.getAttribute().getId();
        this.strValue = av.getStrValue();
        this.intValue =    av.getIntValue();
        this.floatValue =  av.getFloatValue();
        this.doubleValue = av.getDoubleValue();
        this.boolValue =   av.getBoolValue();
        this.dateValue =   av.getDateValue();
    }
}
