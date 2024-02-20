package gp.wagner.backend.domain.dto.request.admin_panel;

import lombok.*;

import java.util.Date;

//Значение атрибута = значение характеристики варианта товара
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatesRangeRequestDto {

    //Нижняя граница диапазона
    @NonNull
    private Date min;

    //Верхняя граница диапазона
    @NonNull
    private Date max;

    public boolean isCorrect(){
        return min.before(max);
    }

}
