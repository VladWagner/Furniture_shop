package gp.wagner.backend.domain.dto.request.crud.reviews;

import jakarta.annotation.Nullable;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImageDto {

    @Nullable
    private Long id;

    @NonNull
    private String fileName;

    //Порядковый номер
    @NonNull
    private Integer imgOrder;


}
