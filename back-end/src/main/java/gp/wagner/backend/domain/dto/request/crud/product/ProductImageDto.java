package gp.wagner.backend.domain.dto.request.crud.product;

import jakarta.annotation.Nullable;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {

    //Для редактирования
    @Nullable
    private Long id;

    @NonNull
    private String fileName;

    //Порядковый номер
    @NonNull
    private Integer imgOrder;


}
