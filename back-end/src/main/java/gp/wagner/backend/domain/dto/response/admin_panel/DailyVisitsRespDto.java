package gp.wagner.backend.domain.dto.response.admin_panel;

import gp.wagner.backend.infrastructure.SimpleTuple;
import lombok.*;

import java.util.Date;

// DTO для передачи кол-ва посещений за каждый день
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyVisitsRespDto {

    // Дата
    @NonNull
    private Date date;

    // Кол-во посещений
    @NonNull
    private Long viewsCount;

    public DailyVisitsRespDto(SimpleTuple<Date, Long> tuple) {
        this.date = tuple.getValue1();
        this.viewsCount = tuple.getValue2();
    }
}
