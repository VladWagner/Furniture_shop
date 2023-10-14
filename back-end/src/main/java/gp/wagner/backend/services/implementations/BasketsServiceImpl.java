package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.entites.basket.Basket;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.repositories.BasketsRepository;
import gp.wagner.backend.services.interfaces.BasketsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BasketsServiceImpl implements BasketsService {

    //Репозиторий
    private BasketsRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setRepository(BasketsRepository repository) {
        this.repository = repository;
    }

    @Override
    public long create(Basket basket) {

        if (basket == null)
            return -1;

        return repository.saveAndFlush(basket).getId();
    }

    @Override
    public long create(long productVariantId, int userId, int products_count, Date addingDate) {

        //Сформировать объект сущности
        /*Basket basket = new Basket(null,Services.productVariantsService.getById(productVariantId),
                products_count, addingDate,new User());

        return repository.saveAndFlush(basket).getId();*/

        //Basket existingBasket = repository.getBasketByUserAndProductVariant(productVariantId, userId);
        Basket existingBasket = findByProdVariantIdAndUserId(productVariantId, userId);

        if (existingBasket != null)
        {
            existingBasket.setProductsAmount(products_count);
            repository.saveAndFlush(existingBasket);

            return repository.saveAndFlush(existingBasket).getId();
        }

        repository.insertBasket(productVariantId, userId, products_count, addingDate);

        return repository.getMaxId();
    }

    //Выборка корзины по id пользователя и варианта товара
    private Basket findByProdVariantIdAndUserId(long pvId, int userId){

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        //Объект для формирования запросов к БД
        CriteriaQuery<Basket> query = cb.createQuery(Basket.class);

        //Составная таблица - корзина
        Root<Basket> root = query.from(Basket.class);

        //Присоединить таблицу вариантов товара
        Join<Basket, ProductVariant> productVariantJoin = root.join("productVariant");
        Join<Basket, User> userJoin = root.join("user");

        //Условие для выборки
        Predicate predicate = cb.and(
                cb.equal(productVariantJoin.get("id"), pvId),
                cb.equal(userJoin.get("id"), userId)
        );

        query.where(predicate);

        List<Basket> resultList = entityManager.createQuery(query).getResultList();

        if (resultList.size() > 0)
            return resultList.get(0);
        else
            return null;

    }

    @Override
    public void update(Basket basket) {

        if (basket == null)
            return;

        repository.saveAndFlush(basket);

    }

    @Override
    public void update(long basketId, long productVariantId, int userId, int products_count, Date addingDate) {

        repository.updateBasket(basketId, productVariantId, userId, products_count,addingDate);

    }

    @Override
    public List<Basket> getAll() {
        return repository.findAll();
    }

    @Override
    public Basket getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    //Выборка корзин для конкретного пользователя
    @Override
    public List<Basket> getByUserId(int userId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Basket> query = cb.createQuery(Basket.class);

        Root<Basket> root = query.from(Basket.class);

        Join<Basket, User> userJoin = root.join("user");

        Predicate predicate = cb.equal(userJoin.get("id"), userId);

        query.where(predicate);

        List<Basket> baskets = entityManager.createQuery(query).getResultList();

        if (baskets != null && baskets.size() > 0)
            return baskets;
        else
            return null;
    }

    @Override
    public List<Basket> getByProductId(long productVariantId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        //Объект для формирования запросов к БД
        CriteriaQuery<Basket> query = cb.createQuery(Basket.class);

        //Составная таблица - корзина
        Root<Basket> root = query.from(Basket.class);

        //Присоединить таблицу вариантов товара
        Join<Basket, ProductVariant> productVariantJoin = root.join("productVariant");

        //Условие для выборки
        Predicate predicate = cb.equal(productVariantJoin.get("id"), productVariantId);

        query.where(predicate);

        List<Basket> baskets = entityManager.createQuery(query).getResultList();

        return baskets.size() > 0 ? baskets : null;
    }

    @Override
    public long getMaxId() {
        return repository.getMaxId();
    }

    @Override
    public long deleteBasketByUserAndProdVariant(int userId, long productId) {

        Basket existingBasket = findByProdVariantIdAndUserId(productId, userId);

        //Проверить, существует ли корзина
        if (existingBasket != null){
            repository.delete(existingBasket);

            return existingBasket.getId();
        }

        return 0;
    }
}
