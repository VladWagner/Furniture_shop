package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.BasketRequestDto;
import gp.wagner.backend.domain.entites.baskets.Basket;
import gp.wagner.backend.domain.entites.baskets.BasketAndProductVariant;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.repositories.baskets.BasketsAndProductVariantsRepository;
import gp.wagner.backend.repositories.baskets.BasketsRepository;
import gp.wagner.backend.repositories.UsersRepository;
import gp.wagner.backend.repositories.products.ProductVariantsRepository;
import gp.wagner.backend.services.interfaces.BasketsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BasketsServiceImpl implements BasketsService {

    @PersistenceContext
    private EntityManager entityManager;

    //region Репозитории
    private BasketsRepository basketsRepository;

    @Autowired
    public void setBasketsRepository(BasketsRepository basketsRepository) {
        this.basketsRepository = basketsRepository;
    }

    // Репозиторий таблицы многие ко многим
    private BasketsAndProductVariantsRepository bpvRepository;

    @Autowired
    public void setBasketsRepository(BasketsAndProductVariantsRepository bpvRepository) {
        this.bpvRepository = bpvRepository;
    }

    // Репозиторий таблицы вариантов товаров
    private ProductVariantsRepository productVariantsRepository;

    @Autowired
    public void setBasketsRepository(ProductVariantsRepository productVariantsRepository) {
        this.productVariantsRepository = productVariantsRepository;
    }

    // Репозиторий для пользователей
    private UsersRepository usersRepository;

    @Autowired
    public void setBasketsRepository(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }
    //endregion

    @Override
    public long create(Basket basket) {

        if (basket == null)
            return -1;

        return basketsRepository.saveAndFlush(basket).getId();
    }

    @Override
    public long create(long productVariantId, int userId, int products_count, Date addingDate) {

        //Basket existingBasket = findByProdVariantIdAndUserId(productVariantId, userId);
        Basket existingBasket = ServicesUtils.findByProdVariantIdAndUserIdGeneric(productVariantId, null, userId, entityManager, Basket.class);

        if (existingBasket != null)
        {
            /*existingBasket.setProductsAmount(products_count);
            basketsRepository.saveAndFlush(existingBasket);*/

            return -1;
        }

        basketsRepository.insertBasket(userId);

        return basketsRepository.getMaxId();
    }

    @Override
    public long create(BasketRequestDto dto) {

        if (dto == null || dto.getUserId() == null || dto.getProductVariantIdAndCount() == null)
            return 0;

        // Найти пользователя, для которого создаётся корзина
        User user = usersRepository.findById(dto.getUserId()).orElse(null);

        if (user == null)
            return 0;

        Basket basket = getByUserId(dto.getUserId());

        // Если корзина для пользователя уже существует
        if (basket != null) {
            // Сбросить сумму и удалить все варианты товаров
            basket.setSum(0);

            bpvRepository.deleteBasketAndProductVariantsByBasketId(basket.getId());
        }
        else {
            basket = new Basket(dto.getId(), null, user, null, 0);

            basket = basketsRepository.saveAndFlush(basket);
        }

        // Сформировать список товаров в корзине пользователя + подсчитать общую сумму в корзине
        List<BasketAndProductVariant> bpvList = addProductVariants(dto.getProductVariantIdAndCount().entrySet(), basket);

        bpvRepository.saveAll(bpvList);

        return basket.getId();
    }

    @Override
    public void insertProductVariants(BasketRequestDto basketDto) {
        Basket foundBasket = basketDto.getId() != null ?
                basketsRepository.findById(basketDto.getId()).orElseThrow() :
                basketDto.getUserId() != null ? getByUserId(basketDto.getUserId()) : null;

        if (foundBasket == null)
            throw new ApiException("Корзина не найдена!");
        else if (basketDto.getProductVariantIdAndCount() == null)
            throw new ApiException("Варианты товаров для добавления в корзину не заданы!");

        List<BasketAndProductVariant> basketAndProductVariants = addAndUpdateProductVariants(basketDto.getProductVariantIdAndCount().entrySet(), foundBasket);

        bpvRepository.saveAll(basketAndProductVariants);

    }

    // Добавление вариантов товаров для корзины
    private List<BasketAndProductVariant> addProductVariants(Set<Map.Entry<Integer, Integer>> pvEntry, Basket basket){
        // Сформировать список товаров в корзине пользователя + подсчитать общую сумму в корзине
        List<BasketAndProductVariant> bpvList = new LinkedList<>();

        int totalSum = 0;

        ProductVariant productVariant;

        for (Map.Entry<Integer, Integer> entry: pvEntry) {
            productVariant = productVariantsRepository.findById(entry.getKey().longValue()).orElse(null);

            if(productVariant == null)
                continue;

            bpvList.add(new BasketAndProductVariant(null, productVariant, entry.getValue(), basket));

            totalSum += productVariant.getPrice()*entry.getValue();
        }

        basket.setSum(totalSum);
        //basket.setBasketAndPVList(bpvList);

        update(basket);

        return bpvList;
    }

    // Добавление и редактирование вариантов товаров для корзины
    private List<BasketAndProductVariant> addAndUpdateProductVariants(Set<Map.Entry<Integer, Integer>> pvEntry, Basket basket){

        // Если id корзины не задано, тогда это добавление вариантов товаров в новую корзину
        if (basket.getId() == null)
            return addProductVariants(pvEntry, basket);

        // Сформировать список товаров в корзине пользователя + подсчитать общую сумму в корзине
        List<BasketAndProductVariant> newBpvList = new LinkedList<>();

        // Получить список вариантов товаров для заданной корзины
        List<BasketAndProductVariant> oldBpvList = bpvRepository.findBasketAndProductVariantsByBasketId(basket.getId());

        int totalSum = basket.getSum();

        ProductVariant productVariant;
        BasketAndProductVariant oldBpv;

        for (Map.Entry<Integer, Integer> entry: pvEntry) {
            productVariant = productVariantsRepository.findById(entry.getKey().longValue()).orElse(null);

            if(productVariant == null)
                continue;

            ProductVariant finalProductVariant = productVariant;

            // Найти существующую запись таблицы м к м для варианта товара
            oldBpv = oldBpvList.stream()
                    .filter(bpv -> bpv.getProductVariant().getId().equals(finalProductVariant.getId()))
                    .findFirst()
                    .orElse(null);

            // Сохранить/добавить вариант товара и пересчитать общую сумму
            if (oldBpv != null) {

                int oldSumPart = oldBpv.getProductsAmount() * productVariant.getPrice();

                // Вычесть часть старой суммы для конкретного варианта товара и его количества
                if (totalSum >= oldSumPart)
                    totalSum -= oldSumPart;

                oldBpv.setProductsAmount(entry.getValue());
                newBpvList.add(oldBpv);

                // Расчитать новую часть суммы
                totalSum += productVariant.getPrice() * oldBpv.getProductsAmount();
            }
            else {
                newBpvList.add(new BasketAndProductVariant(null, productVariant, entry.getValue(), basket));
                totalSum += productVariant.getPrice() * entry.getValue();
            }

        }

        basket.setSum(totalSum);
        //basket.setBasketAndPVList(newBpvList);

        update(basket);

        return newBpvList;
    }

    //Выборка корзины по id пользователя и/или варианта товара
    private Basket findByProdVariantIdAndUserId(Long pvId, Integer userId){

        if (pvId == null && userId == null)
            return null;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        //Объект для формирования запросов к БД
        CriteriaQuery<Basket> query = cb.createQuery(Basket.class);

        //Составная таблица - корзина
        Root<Basket> root = query.from(Basket.class);

        //Присоединить таблицу вариантов товара
        Join<Basket, BasketAndProductVariant> bpvJoin = pvId != null ? root.join("basketAndPVList") : null;
        Path<ProductVariant> productVariantPath = pvId != null ? bpvJoin.get("productVariant") : null;

        Join<Basket, User> userJoin = userId != null ? root.join("user") : null;

        //Условие для выборки если заданы оба параметра
        Predicate predicate = pvId != null && userId != null ? cb.and(
                cb.equal(productVariantPath.get("id"), pvId),
                cb.equal(userJoin.get("id"), userId)
        ) : null;

        // Если не все параметры заданы, только id варианта или пользователя
        if (pvId != null && userId == null)
            predicate = cb.equal(productVariantPath.get("id"), pvId);
        else if (pvId == null)
            predicate = cb.equal(userJoin.get("id"), userId);

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

        basketsRepository.saveAndFlush(basket);

    }

    @Override
    public void update(long basketId, long productVariantId, int userId, int products_count, Date addingDate) {

        basketsRepository.updateBasket(basketId, userId,addingDate);

    }

    @Override
    public void updateBasketsOnPvPriceChanged(ProductVariant changedPv) {

        // Найти корзины с текущим вариантом для изменения суммы
        List<Basket> basketsToChange = ServicesUtils.findByProdVariantIdAndUserIdGeneric(changedPv.getId(), null, null, entityManager ,List.class);

        if (basketsToChange == null)
            return;

        // Найти записи BasketAndProductVariant для получения кол-ва единиц определённого варианта
        List<BasketAndProductVariant> bpvListAll = bpvRepository.findBasketAndProductVariantsByBasketIdsList(basketsToChange.stream().map(Basket::getId).toList());

        // Для каждой корзин изменить общую сумму

        ServicesUtils.countSumInBaskets(basketsToChange, bpvListAll);

        // Сохранить изменения в корзинах
        basketsRepository.saveAllAndFlush(basketsToChange);

    }

    // Обработка сктрия одного или нескольки товароа
    @Override
    public void updateBasketsOnPvHidden(ProductVariant pv, List<ProductVariant> changedPvList) {

        if (pv == null && changedPvList == null)
            return;

        List<Long> changedPvIdsList = changedPvList != null ? changedPvList.stream().map(ProductVariant::getId).toList() : null;
        Long singlePvId = pv != null ? pv.getId() : null;

        // Найти корзины с текущим вариантом для пересчёта суммы
        List<Basket> basketsToChange = ServicesUtils.findByProdVariantIdAndUserIdGeneric(singlePvId, changedPvIdsList,
                null, entityManager ,List.class);

        if (basketsToChange == null)
            return;

        // Найти записи BasketAndProductVariant для всех изменяемых корзин
        List<BasketAndProductVariant> bpvListAll = bpvRepository.findBasketAndProductVariantsByBasketIdsList(basketsToChange.stream().map(Basket::getId).toList());

        bpvListAll = bpvListAll.stream()
                .filter(e -> e.getProductVariant().getShowVariant())
                .toList();

        // Для каждой корзины обновить сумму
        ServicesUtils.countSumInBaskets(basketsToChange, bpvListAll);

        // Сохранить изменения в корзинах
        basketsRepository.saveAllAndFlush(basketsToChange);

    }

    @Override
    public void updateBasketsOnPvDelete(ProductVariant pv, List<ProductVariant> deletedPvList) {
        if (pv == null && deletedPvList == null)
            return;

        List<Long> deletededPvIdsList = deletedPvList != null ? deletedPvList.stream().map(ProductVariant::getId).toList() : null;
        Long singlePvId = pv != null ? pv.getId() : null;

        // Найти корзины с текущим вариантом для пересчёта суммы
        List<Basket> basketsToChange = ServicesUtils.findByProdVariantIdAndUserIdGeneric(singlePvId, deletededPvIdsList,
                null, entityManager ,List.class);

        if (basketsToChange == null)
            return;

        // Найти записи BasketAndProductVariant для всех изменяемых корзин
        List<BasketAndProductVariant> bpvListAll = bpvRepository.findBasketAndProductVariantsByBasketIdsList(basketsToChange.stream().map(Basket::getId).toList());

        // Получить элементы необходимые для удаления
        List<BasketAndProductVariant> bpvListToDelete = bpvListAll.stream()
                .filter(
                        e -> pv != null ?
                        e.getProductVariant().getId().equals(pv.getId()) :
                        deletedPvList.stream()
                                .anyMatch(pv_elem -> pv_elem.getId().equals(e.getProductVariant().getId()))
                )
                .toList();

        // Найти записи с не удаляемыми вариантами
        bpvListAll = bpvListAll.stream()
                .filter(e -> e.getProductVariant().getIsDeleted() == null || !e.getProductVariant().getIsDeleted())
                .toList();
        ServicesUtils.countSumInBaskets(basketsToChange, bpvListAll);

        // Сохранить корзины
        basketsRepository.saveAllAndFlush(basketsToChange);

        // Удалить записи из таблицы многие ко многим
        bpvRepository.deleteAll(bpvListToDelete);

    }

    // Изменение корзин при восстановлении товаров из скрытия
    @Override
    public void updateBasketsOnPvDisclosure(ProductVariant pv, List<ProductVariant> disclosedPvList) {

        updateBasketsOnPvHidden(pv, disclosedPvList);
    }

    @Override
    public Page<Basket> getAll(int pageNumber, int dataOnPage) {
        return basketsRepository.findAll(PageRequest.of(pageNumber-1, dataOnPage));
    }

    @Override
    public Basket getById(Long id) {
        return basketsRepository.findById(id).orElse(null);
    }

    //Выборка корзин для конкретного пользователя
    @Override
    public Basket getByUserId(long userId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Basket> query = cb.createQuery(Basket.class);

        Root<Basket> root = query.from(Basket.class);

        Join<Basket, User> userJoin = root.join("user");

        Predicate predicate = cb.equal(userJoin.get("id"), userId);

        query.where(predicate);

        List<Basket> baskets = entityManager.createQuery(query).getResultList();

        return baskets.size() > 0 ? baskets.get(0) : null;
    }

    @Override
    public List<Basket> getByProductId(long productVariantId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        //Объект для формирования запросов к БД
        CriteriaQuery<Basket> query = cb.createQuery(Basket.class);

        //Составная таблица - корзина
        Root<Basket> root = query.from(Basket.class);

        //Присоединить таблицу вариантов товара
        Join<Basket, BasketAndProductVariant> bpvJoin = root.join("basketAndPVList");

        Path<ProductVariant> productVariantPath = bpvJoin.get("productVariant");

        //Условие для выборки
        Predicate predicate = cb.equal(productVariantPath.get("id"), productVariantId);

        query.where(predicate);

        List<Basket> baskets = entityManager.createQuery(query).getResultList();

        return baskets.size() > 0 ? baskets : null;
    }

    @Override
    public long getMaxId() {
        return basketsRepository.getMaxId();
    }

    // Удалить определённые товары из корзины
    @Override
    public long deleteBasketByUserAndProdVariant(long userId, long productId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        //Объект для формирования запросов к БД
        CriteriaQuery<BasketAndProductVariant> query = cb.createQuery(BasketAndProductVariant.class);

        //Составная таблица - корзина
        Root<BasketAndProductVariant> root = query.from(BasketAndProductVariant.class);

        //Присоединить таблицу вариантов товара
        Join<BasketAndProductVariant, Basket> basketJoin = root.join("basket");

        // Получить путь к атрибуту корзины - вариант товара
        Path<ProductVariant> productVariantPath = root.get("productVariant");

        // Получить путь к атрибуту корзины - пользователь
        Path<User> userPath = basketJoin.get("user");

        //Условие для выборки
        Predicate predicate = cb.and(
                cb.equal(productVariantPath.get("id"), productId),
                cb.equal(userPath.get("id"), userId)
        );

        query.where(predicate);

        BasketAndProductVariant bpv = entityManager.createQuery(query).getResultList().get(0);

        //Проверить, существует ли запись для
        if (bpv != null){

            // Если вариант удаляется и при этом он скрыт, тогда пересчитывать сумму не нужно, поскольку она уже была пересчитана при скрытии варианта,
            // т.е. пользователь прост мог удалить скрытый вариант товара
            if (bpv.getProductVariant().getShowVariant()) {
                Basket editingBasket = getByUserId(userId);

                // Вычесть часть суммы корзин в виде стоимости варианта товара * кол-во товаров
                int newSum = editingBasket.getSum() - (bpv.getProductVariant().getPrice() * bpv.getProductsAmount());

                editingBasket.setSum(newSum);

                update(editingBasket);
            }

            bpvRepository.delete(bpv);

            return bpv.getId();
        }

        return 0;
    }
}
