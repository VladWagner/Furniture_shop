package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entities.visits.DailyVisits;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

@Repository
public interface DailyVisitsRepository extends JpaRepository<DailyVisits,Long> {

    // Добавление записи за день
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into daily_visits
        (date, count)
        values
        (:date, 1)
    """)
    int insertDailyVisits(@Param("date") Date date);

    // Изменение посетителя
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
    update daily_visits set
                        count = count + 1
    where date = :date
    """)
    void increaseVisitsCounter(@Param("date") Date date);

    // Сумма посещений магазина за период
    @Query(nativeQuery = true,
            value = """
    select
        max(dv.count) as max_visits_amount
    from
        daily_visits dv
    where dv.date between :date_lo and :date_hi;
    """)
    long getVisitsSumBetweenDates(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi);

    // Сумма посещений магазина за период
    @Query(nativeQuery = true,
            value = """
    select
        min(dv.count) as min_visits_amount,
        avg(dv.count) as avg_visits_amount,
        max(dv.count) as max_visits_amount
    from
        daily_visits dv
    where dv.date between :date_lo and :date_hi
    """)
    Tuple getQuantityValuesOfVisitsBetweenDates(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi);

    // Получить запись за определённую дату
    @Query(value = """
    select
        dv
    from DailyVisits dv
    where dv.date = :date
""")
    Optional<DailyVisits> getDailyVisitsByDate(@Param("date") LocalDate date);
    // Получить запись за определённую дату
    Optional<DailyVisits> getDailyVisitsByDateIs(Date date);

    //Получить maxId
    @Query(value = """
    select
        max(dv.id)
    from
        DailyVisits dv
    """)
    long getMaxId();
}
