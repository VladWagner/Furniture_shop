package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.Visitor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.bind.Name;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;


public interface VisitorsService {

    //Добавление записи
    long create(Visitor visitor);
    long create(String ipAddress, String fingerPrint);

    Visitor saveIfNotExists(String fingerPrint);
    Visitor saveIfNotExists(String fingerPrint, String ip);

    //Изменение записи
    void update(Visitor visitor);
    void update(long visitorId,@DefaultValue("") String ipAddress, String fingerPrint);
    void updateLastVisitDate(long visitorId, Date visitDate);

    //Выборка всех записей
    List<Visitor> getAll();

    //Выборка записи под id
    Visitor getById(Long id);

    //Выборка записи по fingerPrint
    Visitor getByFingerPrint(String fingerPrint);

    //Выборка записи по fingerPrint и/или по ip
    Visitor getByFingerPrintAndIp(String fingerPrint, String ip);

    //Получение максимального id - последнее добавленное значение
    long getMaxId();

    List<Visitor> getByIdList(List<Long> idsList);
}
