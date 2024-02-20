package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeRequestDto;
import gp.wagner.backend.domain.entites.visits.DailyVisits;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.repositories.DailyVisitsRepository;
import gp.wagner.backend.services.interfaces.DailyVisitsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class DailyVisitsServiceImpl implements DailyVisitsService {

    @PersistenceContext
    private EntityManager entityManager;

    private DailyVisitsRepository dvRepository;

    @Autowired
    public void setDvRepository(DailyVisitsRepository dvRepository) {
        this.dvRepository = dvRepository;
    }

    @Override
    public void increaseCurrentDateCounter() {

        LocalDate now = LocalDate.now();

        DailyVisits dailyVisits = dvRepository.getDailyVisitsByDate(now).orElse(null);

        if (dailyVisits == null)
            dailyVisits = new DailyVisits(now, 1);
        else
            dailyVisits.setCountVisits(dailyVisits.getCountVisits()+1);

        dvRepository.saveAndFlush(dailyVisits);

    }

    @Override
    public void update(DailyVisits dv) {
        if (dv == null || dv.getId() == null)
            throw new ApiException("Запись DailyVisits для редактирования задана некорректно!");

        dvRepository.saveAndFlush(dv);
    }

    @Override
    public Page<DailyVisits> getDailyVisitsBetweenDate(DatesRangeRequestDto datesRangeDto, int pageNum, int dataOnPage) {

        if(datesRangeDto == null || !datesRangeDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        if (pageNum > 0)
            pageNum -= 1;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<DailyVisits> query = cb.createQuery(DailyVisits.class);
        Root<DailyVisits> root = query.from(DailyVisits.class);

        Predicate selectionPredicate = cb.between(root.get("date"), datesRangeDto.getMin(), datesRangeDto.getMax());

        query.where(selectionPredicate);

        TypedQuery<DailyVisits> typedQuery = entityManager.createQuery(query);

        // Пагинация
        typedQuery.setFirstResult(pageNum*dataOnPage);
        typedQuery.setMaxResults(dataOnPage);

        long elementsCount = ServicesUtils.countDailyVisitsInPeriod(entityManager, datesRangeDto);

        List<DailyVisits> dvList = typedQuery.getResultList();

        return new PageImpl<>(dvList,PageRequest.of(pageNum, dataOnPage), elementsCount);
    }

    @Override
    public Tuple getQuantityValuesDailyVisitsBetweenDate(DatesRangeRequestDto datesRangeDto) {

        if(datesRangeDto == null || !datesRangeDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        Tuple rawResult = dvRepository.getQuantityValuesOfVisitsBetweenDates(datesRangeDto.getMin(), datesRangeDto.getMax());

        return rawResult;
    }

    /**
     * Получить дни, когда кол-во просмотров было близко к максимальному в заданный период
     * @param percentage    параметр означает, какой % от максимального количества посещений выбирать
     */
    @Override
    public Page<DailyVisits> getTopDailyVisitsInPeriod(DatesRangeRequestDto datesRangeDto, int pageNum, int dataOnPage, float percentage) {

        if (pageNum > 0)
            pageNum -= 1;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Предварительный запрос для определения максимального числа просмторов
        CriteriaQuery<Integer> countMaxQuery = cb.createQuery(Integer.class);
        Root<DailyVisits> countMaxQueryRoot = countMaxQuery.from(DailyVisits.class);

        Predicate selectionPredicate = cb.between(countMaxQueryRoot.get("date"), datesRangeDto.getMin(), datesRangeDto.getMax());

        countMaxQuery.where(selectionPredicate);

        //  Найти максимальное число посещений в заданном периоде
        countMaxQuery.select(cb.max(countMaxQueryRoot.get("countVisits")));

        int maxVisits = entityManager.createQuery(countMaxQuery).getSingleResult();
        maxVisits = Math.round(maxVisits * (1-percentage));

        CriteriaQuery<DailyVisits> mainQuery = cb.createQuery(DailyVisits.class);
        Root<DailyVisits> root = mainQuery.from(DailyVisits.class);

        // Сформировать предикат для выборки записей посещений за определеённый период и кол-во самих посещений близко к максимальному
        selectionPredicate = cb.between(root.get("date"), datesRangeDto.getMin(), datesRangeDto.getMax());
        selectionPredicate = cb.and(
                selectionPredicate,
                cb.ge(root.get("countVisits"), maxVisits)
        );

        mainQuery.where(selectionPredicate);

        TypedQuery<DailyVisits> typedQuery = entityManager.createQuery(mainQuery);

        // Пагинация
        typedQuery.setFirstResult(pageNum*dataOnPage);
        typedQuery.setMaxResults(dataOnPage);

        long elementsCount = ServicesUtils.countTopDailyVisitsInPeriod(entityManager, datesRangeDto, maxVisits);

        List<DailyVisits> dvList = typedQuery.getResultList();

        return new PageImpl<>(dvList,PageRequest.of(pageNum, dataOnPage), elementsCount);
    }

    @Override
    public Page<DailyVisits> getAll(int pageNum, int dataOnPage) {

        if (pageNum > 0)
            pageNum -= 1;

        return dvRepository.findAll(PageRequest.of(pageNum, dataOnPage));
    }

    @Override
    public DailyVisits getByDate(Date date) {

        if (date == null || date.getTime() > new Date().getTime())
            throw new ApiException("Заданная дата равна null или дата > текущей!");

        return dvRepository.getDailyVisitsByDateIs(date)
                .orElseThrow(() -> new ApiException(String.format("Запись о количестве посетителей за %s не найдена", Utils.sdf.format(date))));
    }

    @Override
    public long getMaxId() {
        return dvRepository.getMaxId();
    }
}
