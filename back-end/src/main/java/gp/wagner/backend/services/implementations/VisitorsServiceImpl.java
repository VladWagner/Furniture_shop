package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.repositories.VisitorsRepository;
import gp.wagner.backend.services.interfaces.VisitorsService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitorsServiceImpl implements VisitorsService {

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

        Visitor visitor = new Visitor(null, ipAddress, fingerPrint);
        /*repository.insertVisitor(ipAddress, fingerPrint);
        return repository.getMaxId();*/
        return repository.saveAndFlush(visitor).getId();

    }
    //endregion

    //region Изменение
    @Override
    public void update(Visitor visitor) {

        if (visitor == null)
            return;

        repository.updateVisitor(visitor.getId(), visitor.getIpAddress(), visitor.getFingerprint());
    }

    @Override
    public void update(long visitorId, String ipAddress, String fingerPrint) {
        if (visitorId <= 0 || fingerPrint.isEmpty())
            return;

        repository.updateVisitor(visitorId, ipAddress, fingerPrint);
    }
    //endregion

    @Override
    public List<Visitor> getAll() {
        return repository.findAll();
    }

    @Override
    public Visitor getById(Long id) {
        return repository.findById(id).get();
    }

    @Override
    public Visitor getByFingerPrint(String fingerPrint) {
        return repository.getVisitorByFingerprint(fingerPrint);
    }

    @Override
    public long getMaxId() {
        return repository.getMaxId();
    }
}
