package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.specifications.ProductSpecifications;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.SortingUtils;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.ProductsSortEnum;
import gp.wagner.backend.repositories.products.ProductsRepository;
import gp.wagner.backend.services.interfaces.SearchService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    @PersistenceContext
    private EntityManager entityManager;

    // Репозиторий
    private ProductsRepository productsRepository;

    @Autowired
    public void setProductsRepository(ProductsRepository prodRepo) {
        this.productsRepository = prodRepo;
    }

    // Поиск по ключевому слову вместе с фильтрацией результатов
    @Override
    public Page<Product> getProductsByKeyword(String key, ProductFilterDtoContainer filterContainer,
                                              String priceRange, int page, int limit,
                                              ProductsSortEnum sortEnum, GeneralSortEnum sortType) {

        if (page > 0)
            page -= 1;

        // Если фильтр будет не задан, тогда просто выборка по ключевому слову
        /*if (filterContainer == null){
            Page<Product> products = productsRepository.findProductsByKeyword(key, PageRequest.of(page, limit));
            return products;
        }*/

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Product> query = cb.createQuery(Product.class).distinct(true);

        Root<Product> root = query.from(Product.class);

        Join<Product, ProductVariant> productVariantJoin = root.join("productVariants");

        //Список предикатов для поиска
        List<Predicate> searchPredicates = new ArrayList<>();

        String preparedKey = "%" + key + "%";
        searchPredicates.add(cb.like(root.get("name"), preparedKey));
        searchPredicates.add(cb.like(root.get("description"), preparedKey));
        searchPredicates.add(cb.like(productVariantJoin.get("title"), preparedKey));
        searchPredicates.add(cb.like(root.get("producer").get("producerName"),  preparedKey));

        // Сформировать предикаты фильтрации по цене и производителям
        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, root, query, filterContainer, null, priceRange,
                ProductsOrVariantsEnum.VARIANTS);

        // Спецификации для фильтрации по характеристикам
        List<Specification<Product>> specifications = ProductSpecifications.createSubQueriesProductSpecifications(filterContainer);
        Predicate featuresPredicate = Specification.allOf(specifications).toPredicate(root, query, cb);

        //Сформировать запрос
        if (predicates != null && !predicates.isEmpty())
            //Доп.фильтра по категории и ценам + фильтра по характеристикам
            query.where(cb.and(
                           cb.and(cb.or(searchPredicates.toArray(new Predicate[0]))
                                   ,featuresPredicate)),
                           cb.and(predicates.toArray(new Predicate[0]))
                    );
        else
            query.where(cb.and( cb.or(searchPredicates.toArray(new Predicate[0])), featuresPredicate));

        // Задать сортировку
        SortingUtils.createSortQueryForProducts(cb, query, root, sortEnum, sortType);

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);

        typedQuery.setMaxResults(limit);
        typedQuery.setFirstResult(page*limit);

        List<Product> products = typedQuery.getResultList();

        // Подсчёт общего кол-ва элементов без пагинации
        long elementsCount = ServicesUtils.countProductsByKeyword(preparedKey, entityManager, specifications, filterContainer, priceRange);

        return new PageImpl<>(products, PageRequest.of(page, limit), elementsCount);
    }

    //Предварительный поиск только при вводе ключевого слова
    @Override
    public List<Product> getProductsPreviewByKeyword(String key) {

        return productsRepository.findProductsByKeyword(key.toLowerCase(), PageRequest.of(0,6)).getContent();
    }

    // Полноценный полнотекстовый поиск с блэкджеком и .... (словоформами, транслитерацией) через hibernate search

}
