package gp.wagner.backend.domain.dto.response.product_attributes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entities.eav.AttributeValue;
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
    @JsonProperty("attribute_value_id")
    private Long attributeValueId;

    @JsonProperty("attribute_id")
    private Long attributeId;

    @JsonProperty("attribute_name")
    private String attributeName;
    @JsonProperty("attribute_priority")
    private float attributePriority;


    //Строковое значение
    @JsonProperty("str_value")
    private String strValue = null;

    //Целочисленное значение
    @JsonProperty("int_value")
    private Integer intValue = null;

    //Значение с плавающей запятой
    @JsonProperty("float_value")
    private Float floatValue = null;

    //Значение double
    @JsonProperty("double_value")
    private Double doubleValue = null;

    //Значение bool
    @JsonProperty("bool_value")
    private Boolean boolValue = null;

    //Значение даты
    @JsonProperty("date_value")
    private Date dateValue = null;

    public AttributeValueRespDto(AttributeValue av) {
        this.attributeValueId = av.getId();
        this.attributeId = av.getAttribute().getId();
        this.attributeName = av.getAttribute().getAttributeName();
        this.attributePriority = av.getAttribute().getPriority();
        this.strValue = av.getStrValue();
        this.intValue =    av.getIntValue();
        this.floatValue =  av.getFloatValue();
        this.doubleValue = av.getDoubleValue();
        this.boolValue =   av.getBoolValue();
        this.dateValue =   av.getDateValue();
    }
}
