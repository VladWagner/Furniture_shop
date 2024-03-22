package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.repositories.VisitorsRepository;
import gp.wagner.backend.services.interfaces.VisitorsService;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class VisitorsServiceImpl implements VisitorsService {

    @PersistenceContext
    private EntityManager entityManager;

    //Репозиторий
    private VisitorsRepository repository;

    @Autowired
    public void setRepository(VisitorsRepository repository) {
        this.repository = repository;
    }


    //region Создание
    @Override
    public long create(Visitor visitor) {
        if (visitor == null)
            return -1;

        //repository.insertVisitor(visitor.getIpAddress(), visitor.getFingerprint());
        return repository.saveAndFlush(visitor).getId();
    }

    @Override
    public long create(@Nullable @DefaultValue("") String ipAddress, String fingerPrint) {
        if (fingerPrint.isBlank() || fingerPrint.isEmpty())
            return -1;

        Visitor visitor = new Visitor(null, ipAddress, fingerPrint, new Date());
        /*repository.insertVisitor(ipAddress, fingerPrint);
        return repository.getMaxId();*/
        return repository.saveAndFlush(visitor).getId();

    }

    @Override
    public Visitor saveIfNotExists(String fingerPrint) {

        Visitor visitor = getByFingerPrint(fingerPrint);

        if (visitor == null)
            visitor = repository.saveAndFlush(new Visitor(null, new Date(), "",  fingerPrint));

        return visitor;
    }

    @Override
    public Visitor saveIfNotExists(String fingerPrint, String ip) {

        Visitor visitor = getByFingerPrintAndIp(fingerPrint, ip);

        if (visitor == null)
            visitor = repository.saveAndFlush(new Visitor(null, new Date(), ip,  fingerPrint));

        return visitor;
    }
    //endregion

    //region Изменение
    @Override
    public void update(Visitor visitor) {

        if (visitor == null)
            return;

        //repository.updateVisitor(visitor.getId(), visitor.getIpAddress(), visitor.getFingerprint());
        repository.saveAndFlush(visitor);
    }

    @Override
    public void update(long visitorId, String ipAddress, String fingerPrint) {
        if (visitorId <= 0 || fingerPrint.isEmpty())
            return;

        repository.updateVisitor(visitorId, ipAddress, fingerPrint, new Date());
    }

    @Override
    public void updateLastVisitDate(long visitorId, Date visitDate) {
        if (visitorId <= 0)
            return;

        repository.updateLastVisitDate(visitorId, visitDate);
    }

    //endregion

    @Override
    public List<Visitor> getAll() {
        return repository.findAll();
    }

    @Override
    public Visitor getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(String.format("Посетитель с id %d не найден!", id)));
    }

    @Override
    public Visitor getByFingerPrint(String fingerPrint) {
        return repository.getVisitorByFingerprint(fingerPrint).orElse(null);
    }

    @Override
    public Visitor getByFingerPrintAndIp(String fingerPrint, String ip) {
        return repository.getVisitorByFingerprintAndIpAddress(fingerPrint, ip).orElse(null);
    }

    @Override
    public long getMaxId() {
        return repository.getMaxId();
    }

    @Override
    public List<Visitor> getByIdList(List<Long> idsList) {
        if (idsList == null)
            throw new ApiException("Найти посетителей по списку id не удалось. Список id задан некорректно!");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Visitor> query = cb.createQuery(Visitor.class);
        Root<Visitor> root = query.from(Visitor.class);

        query.where(root.get("id").in(idsList));

        return entityManager.createQuery(query).getResultList();
    }
}
