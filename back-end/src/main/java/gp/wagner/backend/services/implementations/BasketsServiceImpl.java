package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.BasketRequestDto;
import gp.wagner.backend.domain.entities.baskets.Basket;
import gp.wagner.backend.domain.entities.baskets.BasketAndProductVariant;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.domain.entities.users.User;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.repositories.baskets.BasketsAndProductVariantsRepository;
import gp.wagner.backend.repositories.baskets.BasketsRepository;
import gp.wagner.backend.repositories.UsersRepository;
import gp.wagner.backend.repositories.products.ProductVariantsRepository;
import gp.wagner.backend.security.models.UserDetailsImpl;
import gp.wagner.backend.services.interfaces.BasketsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    public void setUsersRepository(UsersRepository usersRepository) {
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
        Basket existingBasket = ServicesUtils.findBasketByProdVariantIdAndUserIdGeneric(productVariantId, null, userId, entityManager, Basket.class);

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
    public Basket create(BasketRequestDto dto) {

        if (dto == null || /*dto.getUserId() == null ||*/ dto.getProductVariantIdAndCount() == null)
            return null;

        // Найти пользователя, для которого создаётся корзина
        //User user = dto.getUserId() != null ? usersRepository.findById(dto.getUserId()).orElse(null) : ServicesUtils.getUserFromSecurityContext(securityContext);
        User user = ServicesUtils.getUserFromSecurityContext(SecurityContextHolder.getContext());

        if (user == null)
            return null;

        Basket basket = getByUserId(user.getId());

        // Если корзина для пользователя уже существует
        if (basket != null) {
            // Сбросить сумму и удалить все варианты товаров
            basket.setSum(0);
            basket.setAddedDate(new Date());

            bpvRepository.deleteBasketAndProductVariantsByBasketId(basket.getId());
        }
        else {
            basket = new Basket(dto.getId(),  user,  0);

            basket = basketsRepository.saveAndFlush(basket);
        }

        // Сформировать список товаров в корзине пользователя + подсчитать общую сумму в корзине
        List<BasketAndProductVariant> bpvList = addProductVariants(dto.getProductVariantIdAndCount().entrySet(), basket);

        basket.setBasketAndPVList(bpvRepository.saveAll(bpvList));

        return basket;
    }

    @Override
    public Basket insertProductVariants(BasketRequestDto basketDto) {
        if (basketDto.getProductVariantIdAndCount() == null)
            throw new ApiException("Варианты товаров для добавления в корзину не заданы!");

        // Получить id пользователя, чтобы лишний раз к БД не обращаться
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = User.class.isAssignableFrom(principal.getClass()) ? ((User) principal).getId() : ((UserDetailsImpl) principal).getUserId();


        Basket foundBasket = basketDto.getId() != null ?
                basketsRepository.findById(basketDto.getId()).orElseThrow() :
                getByUserId(userId);

        if (foundBasket == null){
            //throw new ApiException("Корзина не найдена!");

            User user = ServicesUtils.getUserFromSecurityContext(SecurityContextHolder.getContext());

            if (user == null)
                throw new ApiException("Не удалось создать корзину, пользователь не задан");

            foundBasket = basketsRepository.saveAndFlush(new Basket(null, null, user, null, 0));

        }

        List<BasketAndProductVariant> basketAndProductVariants = addAndUpdateProductVariants(basketDto.getProductVariantIdAndCount().entrySet(), foundBasket);

        bpvRepository.saveAll(basketAndProductVariants);

        return basketsRepository.findById(foundBasket.getId()).orElse(foundBasket);
    }

    private Map<Long, ProductVariant> getProductsVariantsMapFromEntrySet(Set<Map.Entry<Long, Integer>> pvEntrySet){
        // Получить варианты товаров
        List<Long> pvIdsList = pvEntrySet.stream().map(Map.Entry::getKey).toList();
        List<ProductVariant> productVariants = productVariantsRepository.findProductVariantsByIdList(pvIdsList);

        // Сформировать ассоциативную коллекцию вариантов
        return productVariants.stream().collect(Collectors.toMap(ProductVariant::getId, pv -> pv));
    }

    // Добавление вариантов товаров для корзины
    private List<BasketAndProductVariant> addProductVariants(Set<Map.Entry<Long, Integer>> pvEntrySet, Basket basket){
        // Сформировать список товаров в корзине пользователя + подсчитать общую сумму в корзине
        List<BasketAndProductVariant> bpvList = new LinkedList<>();

        int totalSum = 0;

        // Сформировать ассоциативную коллекцию вариантов
        Map<Long, ProductVariant> pvMap = getProductsVariantsMapFromEntrySet(pvEntrySet);

        ProductVariant productVariant;

        for (Map.Entry<Long, Integer> entry: pvEntrySet) {
            productVariant = pvMap.get(entry.getKey());

            if(productVariant == null)
                continue;

            bpvList.add(new BasketAndProductVariant(null, productVariant, entry.getValue(), basket));

            // Рассчитать сумму по обычной цене, либо цене со скидкой
            totalSum += productVariant.getPriceWithDiscount()*entry.getValue();
        }

        basket.setSum(totalSum);

        update(basket);

        return bpvList;
    }

    // Изменение кол-ва вариантов товаров для корзины
    @Override
    public Basket updateProductVariantCounter(Long pvId, int pvCount){

        Basket basket = getForAuthenticatedUser();

        if (basket == null)
            throw new ApiException("Корзина для пользователя не найдена!");

        BasketAndProductVariant bpv = null; /*basket.getBasketAndPVList()
                .stream()
                .filter(e -> e.getProductVariant().getId().equals(pvId))
                .findFirst()
                .orElse(null);*/

        int foundBpvIdx = -1;

        for (int i = 0; i < basket.getBasketAndPVList().size() ; i++) {
            if (!basket.getBasketAndPVList().get(i).getProductVariant().getId().equals(pvId))
                continue;

            foundBpvIdx = i;
            bpv = basket.getBasketAndPVList().get(i);
        }

        if (bpv != null )
            bpv.setProductsAmount(pvCount);
        else if (pvCount <= 0)
            basket.getBasketAndPVList().remove(foundBpvIdx);
        // Если не нашли запись для нужного варианта товара, тогда добавить вариант товара в корзину и увеличить счётчик
        else {

            ProductVariant pv = productVariantsRepository.findById(pvId).orElse(null);

            // Если варианта товара с таким id нет
            if (pv == null)
                return basket;

            // Добавить запись о варианте в корзину
            bpv = bpvRepository.saveAndFlush(new BasketAndProductVariant(null ,pv, pvCount, basket));
            basket.getBasketAndPVList().add(bpv);

        }

        // Пересчитать сумму корзины
        ServicesUtils.countSumInBasket(basket);

        return basketsRepository.saveAndFlush(basket);
    }

    // Добавление и редактирование вариантов товаров для корзины
    private List<BasketAndProductVariant> addAndUpdateProductVariants(Set<Map.Entry<Long, Integer>> pvEntrySet, Basket basket){

        // Если id корзины не задано, тогда это добавление вариантов товаров в новую корзину
        if (basket.getId() == null)
            return addProductVariants(pvEntrySet, basket);

        // Новый список товаров в корзин
        List<BasketAndProductVariant> newBpvList = new ArrayList<>();

        // Получить список вариантов товаров для заданной корзины и сформировать из него ассоцитивную коллекцию
        //List<BasketAndProductVariant> oldBpvList = bpvRepository.findBasketAndProductVariantsByBasketId(basket.getId());
        Map<Long, BasketAndProductVariant> oldBpvMap = bpvRepository.findBasketAndProductVariantsByBasketId(basket.getId())
                .stream().
                collect(Collectors.toMap(bpv -> bpv.getProductVariant().getId(), bpv -> bpv));


        // Сформировать ассоциативную коллекцию вариантов
        Map<Long, ProductVariant> pvMap = getProductsVariantsMapFromEntrySet(pvEntrySet);

        int totalSum = basket.getSum();

        ProductVariant productVariant;
        BasketAndProductVariant oldBpv;

        // Сформировать список товаров в корзине пользователя + подсчитать общую сумму в корзине
        for (Map.Entry<Long, Integer> entry: pvEntrySet) {
            productVariant = pvMap.get(entry.getKey());

            if(productVariant == null)
                continue;

            oldBpv = oldBpvMap.get(productVariant.getId());

            // Сохранить/добавить вариант товара и пересчитать общую сумму
            if (oldBpv != null) {

                // Используем цену со скидкой, поскольку если эта самая скидка была установлена, то сумма должна быть корректной
                int oldSumPart = oldBpv.getProductsAmount() * productVariant.getPriceWithDiscount();

                // Вычесть часть старой суммы для конкретного варианта товара и его количества
                if (totalSum >= oldSumPart)
                    totalSum -= oldSumPart;

                oldBpv.setProductsAmount(entry.getValue());
                newBpvList.add(oldBpv);

                // Рассчитать новую часть суммы
                totalSum += productVariant.getPrice() * oldBpv.getProductsAmount();
            }
            else {
                newBpvList.add(new BasketAndProductVariant(null, productVariant, entry.getValue(), basket));
                totalSum += productVariant.getPriceWithDiscount() * entry.getValue();
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
    @Transactional
    public Basket updateOrCreate(BasketRequestDto basketDto) {

        if (basketDto == null || basketDto.getProductVariantIdAndCount().isEmpty())
            throw new ApiException("Не удалось изменить корзину. Dto задано некорректно!");

        User user = ServicesUtils.getUserFromSecurityContext(SecurityContextHolder.getContext());

        if (user == null)
            throw new ApiException("Не удалось изменить корзину. Пользователь не аутентифицирован");

        Basket basket = getByUserId(user.getId());

        if (basket == null)
            basket = new Basket(null, user, 0);
            //throw new ApiException("Не удалось изменить корзину. Корзина для пользователя не найдена");

        // Ассоциативная hash коллекция с ключами по id bpv
        Map<Long, BasketAndProductVariant> bpvMap = basket.getBasketAndPVList() != null ? basket.getBasketAndPVList().stream().collect(Collectors.toMap(
                bpv -> bpv.getProductVariant().getId(),
                bpv -> bpv,
                (oldValue, newValue) -> oldValue,
                HashMap::new
        )) : new HashMap<>();

        List<BasketAndProductVariant> bpvToAddOrChange = new ArrayList<>();
        Map<Long, Integer> pvAndCountMap = new HashMap<>();

        for (Map.Entry<Long, Integer> entry: basketDto.getProductVariantIdAndCount().entrySet()){

            // Если у корзины уже есть такой вариант
            if (bpvMap.containsKey(entry.getKey())){

                BasketAndProductVariant foundBpv = bpvMap.get(entry.getKey());

                // Если заданный вариант и его кол-во совпадают с таковыми в таблице
                if (foundBpv.getProductsAmount() == entry.getValue() || entry.getValue() <= 0)
                    continue;

                foundBpv.setProductsAmount(entry.getValue());
                bpvToAddOrChange.add(foundBpv);

            }
            // Если у корзины такого варианта нет, тогда добавить его
            else {
                /*ProductVariant pv = productVariantsRepository.findById(entry.getKey()).orElse(null);

                if (pv != null)
                    bpvToAddOrChange.add(new BasketAndProductVariant(null, pv, entry.getValue(), basket));*/
                pvAndCountMap.put(entry.getKey(), entry.getValue());
            }
        }

        // Добавить объекты BasketAndPv для тех вариантов товаров, которых нет в текущей корзине
        if (!pvAndCountMap.isEmpty()){
            List<ProductVariant> productVariants = productVariantsRepository.findProductVariantsByIdList(pvAndCountMap.keySet().stream().toList());

            if (!productVariants.isEmpty()) {
                for (ProductVariant pv : productVariants) {
                    bpvToAddOrChange.add(new BasketAndProductVariant(null, pv, pvAndCountMap.get(pv.getId()), basket));
                }
            }
        }

        // Удалить из основной map объектов BasketAndPV, все значения в map полученной из DTO, те что останутся в DTO
        // не заданы, следовательно, были удалены на фронте
        bpvMap.keySet().removeAll(basketDto.getProductVariantIdAndCount().keySet());

        if (!bpvMap.isEmpty())
            bpvRepository.deleteAll(bpvMap.values());

        if (!bpvToAddOrChange.isEmpty())
            bpvRepository.saveAllAndFlush(bpvToAddOrChange);

        // Получить обновленную корзину
        basket = basket.getId() != null ? basketsRepository.findById(basket.getId()).orElse(null) : basketsRepository.saveAndFlush(basket);

        if (basket != null && basket.getId() != null) {
            entityManager.refresh(basket);
        }

        ServicesUtils.countSumInBasket(basket);

        return basket;
    }

    @Override
    public void update(long basketId, long productVariantId, int userId, int products_count, Date addingDate) {

        basketsRepository.updateBasket(basketId, userId,addingDate);

    }

    @Override
    public void updateBasketsOnPvPriceChanged(ProductVariant changedPv) {

        // Найти корзины с текущим вариантом для изменения суммы
        List<Basket> basketsToChange = ServicesUtils.findBasketByProdVariantIdAndUserIdGeneric(changedPv.getId(), null, null, entityManager ,List.class);

        if (basketsToChange == null)
            return;

        // Найти записи BasketAndProductVariant для получения кол-ва единиц определённого варианта
        List<BasketAndProductVariant> bpvListAll = bpvRepository.findBasketAndProductVariantsByBasketIdsList(basketsToChange.stream().map(Basket::getId).toList());

        // Для каждой корзин изменить общую сумму

        ServicesUtils.countSumInBaskets(basketsToChange, bpvListAll);

        // Сохранить изменения в корзинах
        basketsRepository.saveAllAndFlush(basketsToChange);

    }

    // Обработка скрытия одного или нескольких товаров
    @Override
    public void updateBasketsOnPvHidden(ProductVariant pv, List<ProductVariant> changedPvList) {

        if (pv == null && changedPvList == null)
            return;

        List<Long> changedPvIdsList = changedPvList != null ? changedPvList.stream().map(ProductVariant::getId).toList() : null;
        Long singlePvId = pv != null ? pv.getId() : null;

        // Найти корзины с текущим вариантом для пересчёта суммы
        List<Basket> basketsToChange = ServicesUtils.findBasketByProdVariantIdAndUserIdGeneric(singlePvId, changedPvIdsList,
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
        List<Basket> basketsToChange = ServicesUtils.findBasketByProdVariantIdAndUserIdGeneric(singlePvId, deletededPvIdsList,
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
    public void recountSumsForVariants(Long pvId, List<Long> pvIdList) {
        // Найти корзины с текущим вариантом для изменения суммы
        List<Basket> basketsToChange = ServicesUtils.findBasketByProdVariantIdAndUserIdGeneric(pvId, pvIdList, null, entityManager , List.class);

        if (basketsToChange == null)
            return;

        // Найти записи BasketAndProductVariant для получения кол-ва единиц определённого варианта
        List<BasketAndProductVariant> bpvListAll = bpvRepository.findBasketAndProductVariantsByBasketIdsList(basketsToChange.stream().map(Basket::getId).toList());

        // Для каждой корзин изменить общую сумму

        ServicesUtils.countSumInBaskets(basketsToChange, bpvListAll);

        // Сохранить изменения в корзинах
        basketsRepository.saveAllAndFlush(basketsToChange);
    }

    @Override
    public Basket getForAuthenticatedUser() {

        User user = ServicesUtils.getUserFromSecurityContext(SecurityContextHolder.getContext());

        if (user == null)
            throw new ApiException("Не удалось найти корзину! Пользователь не аутентифицирован.");

        return getByUserId(user.getId());
    }

    @Override
    public Page<Basket> getAll(int pageNumber, int dataOnPage) {
        return basketsRepository.findAll(PageRequest.of(pageNumber-1, dataOnPage));
    }

    @Override
    public Basket getById(Long id) {

        Basket basket = basketsRepository.findById(id).orElse(null);

        // Текущий авторизированный пользователь
        User user = ServicesUtils.getUserFromSecurityContext(SecurityContextHolder.getContext());

        // Если аутентифицированный и авторизированный пользователь имеет базовую роль и при этом запрашивает не его корзину
        if ((basket != null && user != null) &&
            user.getUserRole().getRole().equals(Constants.BASIC_USER_ROLE.getRole()) && !user.getId().equals(basket.getUser().getId()))
            throw new ApiException(String.format("Пользователь %s не имеет достаточно прав для просмотра корзины с id: %d",
                            user.getUserLogin(), basket.getId()), HttpStatus.FORBIDDEN);

        return basket;
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

        List<BasketAndProductVariant> bpvList = entityManager.createQuery(query).getResultList();

        BasketAndProductVariant bpv = bpvList != null && bpvList.size() > 0 ? bpvList.get(0) : null;

        //Проверить, существует ли запись варианта товара
        if (bpv != null){

            bpvRepository.delete(bpv);

            Basket editingBasket = getByUserId(userId);

            // Если вариант удаляется и при этом он скрыт, тогда пересчитывать сумму не нужно, поскольку она уже была пересчитана при скрытии варианта,
            if (bpv.getProductVariant().getShowVariant()) {

                ServicesUtils.countSumInBasket(editingBasket);

                update(editingBasket);
            }

            return bpv.getId();
        }

        return 0;
    }

    // Удалить определённые товары из корзины
    @Override
    public Basket deleteBasketByAuthUserAndProdVariant(long pvId) {

        User user = ServicesUtils.getUserFromSecurityContext(SecurityContextHolder.getContext());

        if (user == null)
            throw new ApiException("Не удалось найти корзину! Пользователь не аутентифицирован.");

        return deleteBasketByUserAndProdVariant(user.getId(), pvId) > 0 ? getByUserId(user.getId()) : null;
    }
}
