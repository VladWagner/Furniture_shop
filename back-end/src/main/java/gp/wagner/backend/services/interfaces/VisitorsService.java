package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.Visitor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.bind.Name;

import java.util.List;


public interface VisitorsService {

    //Добавление записи
    long create(Visitor visitor);
    long create(String ipAddress, String fingerPrint);

    //Изменение записи
    void update(Visitor visitor);
    void update(long visitorId,@DefaultValue("") String ipAddress, String fingerPrint);

    //Выборка всех записей
    public List<Visitor> getAll();

    //Выборка записи под id
    Visitor getById(Long id);

    //Выборка записи по fingerPrint
    Visitor getByFingerPrint(String fingerPrint);

    //Получение максимального id - последнее добавленное значение
    long getMaxId();

}
