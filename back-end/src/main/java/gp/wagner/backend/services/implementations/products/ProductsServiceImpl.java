package gp.wagner.backend.services.implementations.products;

import gp.wagner.backend.domain.dto.request.crud.product.ProductDto;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.dto.response.filters.FilterValuesDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.domain.specifications.ProductSpecifications;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.products.ProductsRepository;
import gp.wagner.backend.services.interfaces.products.ProductsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

// Сервис для таблицы товаров
@Service
public class ProductsServiceImpl implements ProductsService {

    // Репозиторий
    private ProductsRepository productsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setProductsRepository(ProductsRepository prodRepo) {
        this.productsRepository = prodRepo;
    }

    //region Добавление
    @Override
    // Добавление записи
    public void create(Product product){
        if(product == null)
            return;

        productsRepository.saveAndFlush(product);
    }

    // Добавление товара из DTO
    @Override
    public long create(ProductDto dto) {
      if (dto == null)
          return -1;

        productsRepository.insertProduct(dto.getName(), dto.getDescription(),dto.getCategoryId().intValue(),
                dto.getProducerId().intValue(),
                dto.getIsAvailable() ? 1 : 0, dto.getShowProduct() ? 1 : 0);

        return productsRepository.getMaxId();
    }
    //endregion

    //region Изменение
    @Override
    public void update(Product item) {
        if(item == null)
            return;

        productsRepository.saveAndFlush(item);

    }

    // Из DTO
    @Override
    public void update(ProductDto dto) {
        if (dto == null)
            return;

        productsRepository.updateProduct(dto.getId(), dto.getName(), dto.getDescription(),
                dto.getCategoryId(), dto.getProducerId(),
                dto.getIsAvailable() ? 1 : 0, dto.getShowProduct() ? 1 : 0);
    }

    //endregion

    @Override
    // Выборка всех записей
    public List<Product> getAll(){return productsRepository.findAll();}

    @Override
    public long getMaxId() {
        return productsRepository.getMaxId();
    }

    // Выборка с пагинацией
    @Override
    public Page<Product> getAll(int pageNum, int dataOnPage) {

        return productsRepository.findAllNotDeleted(PageRequest.of(pageNum, dataOnPage));
    }

    // Фильтрация и пагинация
    @Override
    public Page<Product> getAll(ProductFilterDtoContainer container, Long categoryId, String priceRange,
                                                      int pageNum, int dataOnPage) {

        // Сформировать набор спецификаций для выборки из набора фильтров (фильтр = атрибут (характеристика) + операция)
        List<Specification<Product>> specifications = ProductSpecifications.createSubQueriesProductSpecifications(container);

        // Объект для формирования запросов - построитель запроса
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Рассчитать общее кол-во данных с такими фильтрами
        CriteriaQuery<Product> query = cb.createQuery(Product.class);

        // Получить таблицу товаров для запросов
        Root<Product> root = query.from(Product.class);

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, root, query, container, categoryId, priceRange, ProductsOrVariantsEnum.PRODUCTS);

        // Получить предикат для выборки по заданным фильтрам (контейнер фильтров, который был передан из контроллера)
        // При помощи данного предиката запросы будут строится со стороны Spring data & Hibernate
        // Метод toPredicate - по-идее использует то анонимное создание спецификации, которое задано в ProductSpecifications.java
        Predicate filterPredicate = Specification.allOf(specifications).toPredicate(root, query, cb);

        // Сформировать запрос
        if (predicates != null && !predicates.isEmpty())
            //Доп.фильтра по категории и ценам + фильтра по характеристикам
            query.where(cb.and(
                    cb.and(predicates.toArray(new Predicate[0])), filterPredicate));
        else
            query.where(filterPredicate);


        if (pageNum > 0)
            pageNum -= 1;

        //Данный объект нужен для пагинации полученных после выборки результатов
        TypedQuery<Product> typedQuery = entityManager.createQuery(query);

        //region Пагинация через TypedQuery
        // Задать кол-во результатов на странице
        typedQuery.setMaxResults(dataOnPage);

        // Задать смещение на кол-во страниц
        typedQuery.setFirstResult(pageNum*dataOnPage);
        //endregion

        List<Product> products = typedQuery.getResultList();

        // Пагинация готовой коллекции
        long elementsCount = ServicesUtils.countProductsByFilter(entityManager, specifications, container, categoryId, priceRange);

        return new PageImpl<>(products, PageRequest.of(pageNum, dataOnPage), elementsCount);
    }

    @Override
    public Page<Product> getAllWithCorrectPrices(ProductFilterDtoContainer filtersContainer, Long categoryId, String priceRange, int pageNum, int dataOnPage) {

        List<Specification<Product>> specifications = ProductSpecifications.createSubQueriesProductSpecifications(filtersContainer);

        // Объект для формирования запросов - построитель запроса
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Используется установка флага distinct для корректной пагинации, иначе выбирается несоответствующее реальности количество записей
        CriteriaQuery<Product> query = cb.createQuery(Product.class).distinct(true);
        Root<Product> root = query.from(Product.class);

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, root, query, filtersContainer, categoryId, priceRange,
                ProductsOrVariantsEnum.VARIANTS);

        // Получить предикат для выборки по заданным фильтрам
        Predicate filterPredicate = Specification.allOf(specifications).toPredicate(root, query, cb);

        // Сформировать запрос
        if (predicates != null && !predicates.isEmpty())
            //Доп.фильтра по категории и ценам + фильтра по характеристикам
            query.where(cb.and(
                    cb.and(predicates.toArray(new Predicate[0])), filterPredicate));
        else
            query.where(filterPredicate);

        if (pageNum > 0)
            pageNum -= 1;

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(dataOnPage);
        typedQuery.setFirstResult(pageNum*dataOnPage);

        List<Product> products = typedQuery.getResultList();

        // Пагинация готовой коллекции
        long elementsCount = ServicesUtils.countProductsByFilterPv(entityManager, specifications, filtersContainer, categoryId, priceRange);

        return new PageImpl<>(products, PageRequest.of(pageNum, dataOnPage), elementsCount);
    }

    @Override
    // Метод для подсчёта кол-ва данных полученных после выборки по определённым фильтрам.
    public long countData(ProductFilterDtoContainer filtersContainer, Long categoryId, String priceRange){

        //Объект для формирования запросов - построитель запроса
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        //Получить таблицу для запросов
        Root<Product> root = query.from(Product.class);

        List<Specification<Product>> specifications = ProductSpecifications.createSubQueriesProductSpecifications(filtersContainer);

        // Собираем предикаты по диапазону цен (варианты товара) и категории
        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, root, query, filtersContainer, categoryId, priceRange,
                ProductsOrVariantsEnum.VARIANTS);

        // Получить предикат для выборки по заданным фильтрам
        Predicate filterPredicate = Specification.allOf(specifications).toPredicate(root, query, cb);

        //Сформировать запрос
        if (predicates != null && !predicates.isEmpty())
            //Доп.фильтра по категории и ценам, производителям + фильтра по характеристикам
            query.where(cb.and(
                    cb.and(predicates.toArray(new Predicate[0])), filterPredicate));
        else
            query.where(filterPredicate);

        query.select(cb.count(root.get("id")));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        //Интересно, на этом моменте происходит этот огромный запрос к БД или всё же он как-то это оптимизирует
        return typedQuery.getResultList().get(0);
    }

    @Override
    // Выборка записи по id
    public Product getById(Long id){
        if (id == null)
            throw new ApiException("Id товара задан некорректно!");

        Optional<Product> productOptional = productsRepository.findById(id);

        return productOptional.orElseThrow(() -> new ApiException(String.format("Не удалось найти товар с id: %d!", id)));
    }

    @Override
    public List<Product> getByIdList(List<Long> idsList) {

        if (idsList == null || idsList.isEmpty())
            throw new ApiException("В метод поиска товаров по списку id передан некорректный список!");

        return productsRepository.findAllById(idsList);
    }

    @Override
    public Page<Product> getByCategory(long categoryId, int pageNum, int dataOnPage) {

        return productsRepository.findProductsByCategoryId(categoryId, PageRequest.of(pageNum, dataOnPage));
    }

    @Override
    public Page<Product> getByProducerPaged(long producerId, int pageNum, int dataOnPage) {
        return productsRepository.findProductsByProducerId(producerId, PageRequest.of(pageNum, dataOnPage));
    }

    // Посчитать, сколько записей в каждой категории
    @Override
    public int countByCategory(long categoryId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        //Таблица товаров
        Root<Product> root = query.from(Product.class);

        //Присоединяем таблицу категорий
        Join<Product, Category> categoryJoin = root.join("category");

        //Посчитать кол-во товаров, где id категории = заданному
        query.select(cb.count(root)).where(cb.equal(categoryJoin.get("id"), categoryId));

        return entityManager.createQuery(query).getSingleResult().intValue();
    }

    // Сформировать объект для выборки пограничных значений цены за товар (для фильтра)
    private FilterValuesDto<Integer> getFilterValueDto(Object rawResult){
        Object[] range = (Object[]) rawResult;

        if (range == null || range.length == 0 || range[0] == null)
            return null;

        return new FilterValuesDto<>(null, "Цена, руб.", null,
                ((Number) range[0]).intValue(), ((Number) range[1]).intValue());
    }

    // Получить диапазон цен на товары в определённой категории
    @Override
    public FilterValuesDto<Integer> getPricesRangeInCategory(long categoryId) {

        return getFilterValueDto(productsRepository.getMinMaxPriceInCategory(categoryId));
    }

    // Получить диапазон цен на товары в нескольких категориях
    @Override
    public FilterValuesDto<Integer> getPricesRangeInCategories(List<Long> categoriesIds) {
        return getFilterValueDto(productsRepository.getMinMaxPriceInCategories(categoriesIds));
    }

    // Получить диапазон цен по ключевому слову (при поиске)
    @Override
    public FilterValuesDto<Integer> getPricesRangeByKeyword(String keyword) {
        return getFilterValueDto(productsRepository.getMinMaxPriceByKeyword(keyword));
    }

    @Override
    public boolean deleteById(long id) {

        Product foundProduct = productsRepository.findById(id)
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти товар с id = %d!",id)));

        if (foundProduct.getIsDeleted())
            throw  new ApiException(String.format("Товар с id: %d уже удалён!", id));

        foundProduct.setIsDeleted(true);

        // Мягко удалить варианты товаров
        Services.productVariantsService.deleteByProductId(id);

        return productsRepository.saveAndFlush(foundProduct).getIsDeleted();
    }

    // Восстановить товар из удаления
    @Override
    public boolean recoverDeletedById(long id, boolean recoverHeirs) {

        Product foundProduct = productsRepository.findById(id)
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти товар с id = %d!",id)));

        if (!foundProduct.getIsDeleted())
            throw  new ApiException(String.format("Товар с id: %d не удалялся!", id));

        foundProduct.setIsDeleted(false);

        // Восстановить удалённые вместе товаром варианты
        if (recoverHeirs)
            Services.productVariantsService.recoverDeletedByProductId(id);

        return productsRepository.saveAndFlush(foundProduct).getIsDeleted();
    }

    @Override
    public void deleteByProducerId(Producer producer) {

        // Если передаваемый producer не был удалён
        if (producer.getDeletedAt() == null)
            return;

        //Найти товары с определёнными производителем
        //List<Product> products = findProductsByProducerByIdAndIsDeletedFlag(producer.getId(), false, null);
        List<Product> products = findProductsByCategoryOrProducerAndIsDeletedFlag(producer.getId(), Producer.class, false, null);

        products.forEach(p -> p.setIsDeleted(true));

        // Мягко удалить все варианты товаров
        Services.productVariantsService.deleteByProductIdList(products.stream().map(Product::getId).toList());

        productsRepository.saveAllAndFlush(products);

    }

    @Override
    public void recoverDeletedByProducerId(Producer producer) {

        // Если восстанавливаемый производитель всё ещё скрыт
        if (producer.getDeletedAt() != null)
            return;

        //List<Product> products = findProductsByProducerByIdAndIsDeletedFlag(producerId, true, null);
        List<Product> products = findProductsByCategoryOrProducerAndIsDeletedFlag(producer.getId(), Producer.class, true, null);

        products.forEach(p -> p.setIsDeleted(false));

        // Восстановить варианты для всех товаров
        Services.productVariantsService.recoverDeletedByProductIdList(products.stream().map(Product::getId).toList());

        productsRepository.saveAllAndFlush(products);
    }

    @Override
    public void hideByProducer(Producer producer) {

        // если производитель не был скрыт
        if (producer.getIsShown())
            return;

        //List<Product> products = findProductsByProducerByIdAndIsDeletedFlag(producer.getId(), false, true);
        List<Product> products = findProductsByCategoryOrProducerAndIsDeletedFlag(producer.getId(), Producer.class, false, true);

        products.forEach(p -> p.setShowProduct(false));

        Services.productVariantsService.hideByProductsList(products);

        productsRepository.saveAllAndFlush(products);
    }

    @Override
    public void recoverHiddenByProducer(Producer producer) {

        if (!producer.getIsShown())
            return;

        // List<Product> products = findProductsByProducerByIdAndIsDeletedFlag(producer.getId(), false, false);
        List<Product> products = findProductsByCategoryOrProducerAndIsDeletedFlag(producer.getId(), Producer.class, false, false);

        products.forEach(p -> p.setShowProduct(true));

        Services.productVariantsService.recoverHiddenByProductsList(products);

    }

    @Override
    public void hideByCategory(Category category) {
        if (category.getIsShown())
            return;

        // Найти товары в категории, которые не были удалены и выводятся
        List<Product> products = findProductsByCategoryOrProducerAndIsDeletedFlag(category.getId(), Category.class, false, true);

        products.forEach(p -> p.setShowProduct(false));

        // Скрыть связанные записи вариантов товаров
        Services.productVariantsService.hideByProductsList(products);

        productsRepository.saveAllAndFlush(products);

    }

    @Override
    public void recoverHiddenByCategory(Category category) {

        // Если категория всё ещё скрыта
        if (!category.getIsShown())
            return;

        // Найти товары в категории, которые не были удалены и скрыты
        List<Product> products = findProductsByCategoryOrProducerAndIsDeletedFlag(category.getId(), Category.class, false, false);

        products.forEach(p -> p.setShowProduct(true));

        // Скрыть связанные записи вариантов товаров
        Services.productVariantsService.recoverHiddenByProductsList(products);

        productsRepository.saveAllAndFlush(products);
    }

    @Override
    public void hideById(long productId) {
        Product product = getById(productId);

        if (!product.getShowProduct())
            throw new ApiException(String.format("Товар с id: %d уже скрыт!", product.getId()));

        product.setShowProduct(false);

        Services.productVariantsService.hideByProductId(product);

        productsRepository.saveAndFlush(product);

    }

    @Override
    public void recoverHiddenById(long productId, boolean recoverHeirs) {
        Product product = getById(productId);

        if (product.getShowProduct())
            throw new ApiException(String.format("Товар с id: %d не был скрыт!", product.getId()));

        product.setShowProduct(true);

        if (recoverHeirs)
            Services.productVariantsService.recoverHiddenByProductId(product);

        productsRepository.saveAndFlush(product);
    }

    // Найти товары по производителю и флагу удаления
    private List<Product> findProductsByProducerByIdAndIsDeletedFlag(long producerId, Boolean isDeleted, Boolean isShown){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Product> query = cb.createQuery(Product.class);

        Root<Product> root = query.from(Product.class);

        // Присоединить производителя
        Path<Producer> producerPath = root.get("producer");

        // Удалённые/Неудалённые товары с определённым производителем
        Predicate predicate = isShown == null && isDeleted == null? cb.equal(producerPath.get("id"), producerId) : null ;

        if (isDeleted != null && isShown != null)
            predicate = cb.and(
                    cb.equal(producerPath.get("id"), producerId),
                    cb.equal(root.get("isDeleted"), isDeleted),
                    cb.equal(root.get("showProduct"), isShown)
            );
        else if (isDeleted != null) {
            predicate = cb.and(
                    cb.equal(producerPath.get("id"), producerId),
                    cb.equal(root.get("isDeleted"), isDeleted)
            );
        }else if (isShown != null) {
            predicate = cb.and(
                    cb.equal(producerPath.get("id"), producerId),
                    cb.equal(root.get("showProduct"), isShown)
            );
        }

        query.where(predicate);

        return entityManager.createQuery(query).getResultList();

    }

    // Найти товары по производителю и флагу удаления
    private List<Product> findProductsByCategoryOrProducerAndIsDeletedFlag(long searchId, Class<?> searchType, Boolean isDeleted, Boolean isShown){

        if (searchType == null || (!searchType.isAssignableFrom(Producer.class) && !searchType.isAssignableFrom(Category.class)))
            return new ArrayList<>();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Product> query = cb.createQuery(Product.class);

        Root<Product> root = query.from(Product.class);

        // Присоединить производителя или категорию в зависимости от заданного типа
        Path<?> searchPath = searchType == Producer.class ? root.get("producer") : root.get("category");

        // Удалённые/Неудалённые товары в определённой категории

        // Задать базовое значение
        Predicate predicate = isShown == null && isDeleted == null ? cb.equal(searchPath.get("id"), searchId) : null;

        if (isDeleted != null && isShown != null)
            predicate = cb.and(
                    cb.equal(searchPath.get("id"), searchId),
                    cb.equal(root.get("isDeleted"), isDeleted),
                    cb.equal(root.get("showProduct"), isShown)
            );
        else if (isDeleted != null) {
            predicate = cb.and(
                    cb.equal(searchPath.get("id"), searchId),
                    cb.equal(root.get("isDeleted"), isDeleted)
            );
        }else if (isShown != null) {
            predicate = cb.and(
                    cb.equal(searchPath.get("id"), searchId),
                    cb.equal(root.get("showProduct"), isShown)
            );
        }

        query.where(predicate);

        return entityManager.createQuery(query).getResultList();

    }

}