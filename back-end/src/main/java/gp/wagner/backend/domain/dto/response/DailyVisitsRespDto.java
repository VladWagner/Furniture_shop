package gp.wagner.backend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.visits.DailyVisits;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

// DTO для передачи записи о посещениях интернет-магазина в конкретный день
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyVisitsRespDto {

    @Nullable
    private Long id;

    @NotNull
    private LocalDate date;

    @NotNull
    @JsonProperty("visits_count")
    private Integer visitsCount;

    public DailyVisitsRespDto(DailyVisits dv) {
        this.id = dv.getId();
        this.date = dv.getDate();
        this.visitsCount = dv.getCountVisits();
    }
}
