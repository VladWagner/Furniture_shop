package gp.wagner.backend.domain.entities.visits;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

//Посетитель
@Entity
@Table(name = "daily_visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyVisits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Дата подсчёта просмотров
    @Column(name = "date")
    private LocalDate date;

    // Кол-во посещений магазина в определённую дату
    @Column(name = "count")
    private Integer countVisits;

    public DailyVisits(LocalDate date, Integer countVisits) {
        this.id = null;
        this.date = date;
        this.countVisits = countVisits;
    }
}
