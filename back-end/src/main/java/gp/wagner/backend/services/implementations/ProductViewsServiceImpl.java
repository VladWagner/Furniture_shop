package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.CategoriesRepository;
import gp.wagner.backend.repositories.ProductViewsRepository;
import gp.wagner.backend.services.interfaces.ProductViewsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductViewsServiceImpl implements ProductViewsService {

    //Репозиторий
    private ProductViewsRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setRepository(ProductViewsRepository repository) {
        this.repository = repository;
    }


    //region Создание
    @Override
    public void create(ProductViews productView) {

        //Проверить наличие записи о посетителе в таблице

        repository.saveAndFlush(productView);
    }

    @Override
    public void create(long visitorId, long productId, int count) {

        if (productId <= 0 || count <= 0 || visitorId <= 0)
            return;

        repository.insertProductView(visitorId, productId, count);

    }

    @Override
    public void createOrUpdate(String fingerPrint, long productId) {
        if (productId <= 0 || fingerPrint.isBlank())
            return;

        //Найти или создать посетителя с заданным отпечатком браузера
        Visitor visitor = Services.visitorsService.getByFingerPrint(fingerPrint);

        long createdVisitor = 0;

        if (visitor == null) {
            Services.visitorsService.create("", fingerPrint);
            createdVisitor = Services.visitorsService.getMaxId();
        }

        //Проверить наличие записи просмотра товара для конкретного посетителя по конкретному товару
        ProductViews productView = getByVisitorAndProductId(visitor != null ? visitor.getId() : createdVisitor, productId);

        //Если запись не существует, тогда создаём, если запись имеется, тогда увеличить счетчик
        if (productView == null)
            repository.insertProductView(visitor == null ? createdVisitor : visitor.getId(), productId, 1);
        else{
            productView.setCount(productView.getCount()+1);
            update(productView);
        }
    }
    //endregion

    //region Изменение
    @Override
    public void update(ProductViews productView) {
        if (productView == null)
            return;

        repository.updateProductView(productView.getId(),productView.getVisitor().getId(),
                productView.getProduct().getId(), productView.getCount());
    }

    @Override
    public void update(long viewId, long visitorId, long productId, int count) {
        if (productId <= 0 || count <= 0 || visitorId <= 0)
            return;

        repository.updateProductView(viewId,visitorId, productId, count);
    }
    //endregion

    @Override
    public List<ProductViews> getAll() {
        return repository.findAll();
    }

    @Override
    public ProductViews getById(Long id) {

        if (id == null)
            return null;

        return repository.findById(id).get();
    }

    @Override
    public ProductViews getByVisitorFingerPrint(String fingerPrint) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<ProductViews> query = cb.createQuery(ProductViews.class);

        //Основная таблица для совершения запросов
        Root<ProductViews> root = query.from(ProductViews.class);

        //Присоединить таблицу посетителей
        Join<ProductViews, Visitor> visitorsJoin = root.join("visitor");

        query.where(cb.equal(visitorsJoin.get("fingerprint"), fingerPrint));

        ProductViews productView = entityManager.createQuery(query).getSingleResult();

        return productView;
    }

    @Override
    public ProductViews getByVisitorAndProductId(long visitorId, long productId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<ProductViews> query = cb.createQuery(ProductViews.class);

        //Основная таблица для совершения запросов
        Root<ProductViews> root = query.from(ProductViews.class);

        //Присоединить таблицу посетителей
        Join<ProductViews, Visitor> visitorsJoin = root.join("visitor");
        Join<ProductViews, Visitor> productsJoin = root.join("product");

        //Выборка по id товара и посетителя
        Predicate predicate = cb.and(
                cb.equal(visitorsJoin.get("id"), visitorId),
                cb.equal(productsJoin.get("id"), productId)
        );

        query.where(predicate);

        List<ProductViews> productViews = entityManager.createQuery(query).getResultList();

        if (productViews.size() != 0)
            return productViews.get(0);
        else
            return null;
    }
}