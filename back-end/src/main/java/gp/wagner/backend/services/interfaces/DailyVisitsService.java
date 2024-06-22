package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeRequestDto;
import gp.wagner.backend.domain.entities.visits.DailyVisits;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;

import java.util.Date;


public interface DailyVisitsService {

    // Увеличить создать счётчик кол-ва просмотров
    void increaseCurrentDateCounter();

    void update(DailyVisits dv);

    // Получить записи в определённом диапазоне дат
    Page<DailyVisits> getDailyVisitsBetweenDate(DatesRangeRequestDto datesRangeDto, int pageNum, int dataOnPage);

    // Получить агрегатные значения количества просмотров за определённый период
    Tuple getQuantityValuesDailyVisitsBetweenDate(DatesRangeRequestDto datesRangeDto);

    /**
     * Получить дни, когда кол-во просмотров было близко к максимальному в заданный период
     * @param percentage параметр означает, какой % от максимального количества посещений выбирать
     * */
    Page<DailyVisits> getTopDailyVisitsInPeriod(DatesRangeRequestDto datesRangeDto, int pageNum, int dataOnPage, float percentage);

    //Выборка всех записей
    Page<DailyVisits> getAll(int pageNum, int dataOnPage);

    //Выборка записи о посещениях за определённый день
    DailyVisits getByDate(Date date);


    //Получение максимального id - последнее добавленное значение
    long getMaxId();

}
