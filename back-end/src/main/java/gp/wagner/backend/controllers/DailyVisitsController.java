package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeAndValRequestDto;
import gp.wagner.backend.domain.dto.response.DailyVisitsRespDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.admin_panel.QuantityValuesRespDto;
import gp.wagner.backend.domain.entities.visits.DailyVisits;
import gp.wagner.backend.middleware.Services;
import jakarta.persistence.Tuple;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/stat/daily_visits")
public class DailyVisitsController {

    // Увеличить счётчик посещений за текущий день
    @PostMapping(value = "/increase_counter")
    public ResponseEntity<?> increaseVisitsCounter() throws Exception{

        Services.dailyVisitsService.increaseCurrentDateCounter();

        return ResponseEntity.ok(true);
    }

    // Получение всех записей с пагинацией
    @GetMapping(value = "/get_all", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DailyVisitsRespDto> getAllDailyVisits(@Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
                                                         @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit) throws Exception{

        Page<DailyVisits> dvPage = Services.dailyVisitsService.getAll(pageNum, limit);

        return new PageDto<>(dvPage, () -> dvPage.getContent().stream().map(DailyVisitsRespDto::new).toList());
    }

    // Получение всех записей о посещениях магазина с кол-вом посещений близким к максимальному
    @GetMapping(value = "/top_daily_visits", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DailyVisitsRespDto> getTopDailyVisits(@Valid @RequestBody DatesRangeAndValRequestDto datesRangeDto,
                                                         @RequestParam(value = "percentage", defaultValue = "0.2") float percentage,
                                                         @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
                                                         @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit) throws Exception{

        Page<DailyVisits> dvPage = Services.dailyVisitsService.getTopDailyVisitsInPeriod(datesRangeDto, pageNum, limit, percentage);

        return new PageDto<>(dvPage, () -> dvPage.getContent().stream().map(DailyVisitsRespDto::new).toList());
    }

    // Максимальное, среднее и минимальное количество посещений
    @GetMapping(value = "/get_min_avg_max_visits", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuantityValuesRespDto> getQuantityValuesDailyVisits(
            @Valid @RequestBody DatesRangeAndValRequestDto datesRangeDto)  {

        Tuple result = Services.dailyVisitsService.getQuantityValuesDailyVisitsBetweenDate(datesRangeDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new QuantityValuesRespDto(result));
    }

}
