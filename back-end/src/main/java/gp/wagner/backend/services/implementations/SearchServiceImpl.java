package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.specifications.ProductSpecifications;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.repositories.ProductsRepository;
import gp.wagner.backend.services.interfaces.SearchService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    // Репозиторий
    private ProductsRepository productsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setProductsRepository(ProductsRepository prodRepo) {
        this.productsRepository = prodRepo;
    }

    // Поиск по ключевому слову вместе с фильтрацией результатов
    @Override
    public SimpleTuple<Integer, List<Product>> getProductsByKeyword(String key, ProductFilterDtoContainer container,
                                                                    String priceRange, int page, int limit) {
        // Если фильтр будет не задан, тогда просто выборка по ключевому слову
        if (container == null){
            Page<Product> products = productsRepository.findProductsByKeyword(key, PageRequest.of(page-1, limit));
            return new SimpleTuple<>((int) products.getTotalElements(), products.getContent());
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Product> query = cb.createQuery(Product.class);

        Root<Product> root = query.from(Product.class);

        Join<Product, ProductVariant> productVariantJoin = root.join("productVariants");

        //Список предикатов для поиска
        List<Predicate> searchPredicates = new ArrayList<>();

        searchPredicates.add(cb.like(root.get("name"),"%" + key + "%"));
        searchPredicates.add(cb.like(root.get("description"),"%" + key + "%"));
        searchPredicates.add(cb.like(productVariantJoin.get("title"),"%" + key + "%"));
        searchPredicates.add(cb.like(root.get("producer").get("producerName"), "%" + key + "%"));

        // Сфорировать предикаты фильтрации по цене и производителям
        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, root, query, container, null, priceRange);

        // Спецификации для фильтрации по характеристикам
        List<Specification<Product>> specifications = ProductSpecifications.createSubQueriesProductSpecifications(container);
        Predicate featuresPredicate = Specification.allOf(specifications).toPredicate(root, query, cb);

        //Сформировать запрос
        if (predicates.size() > 0)
            //Доп.фильтра по категории и ценам + фильтра по характеристикам
            query.where(cb.and(
                           cb.and(cb.or(searchPredicates.toArray(new Predicate[0]))
                                   ,featuresPredicate)),
                           cb.and(predicates.toArray(new Predicate[0]))
                    ).distinct(true);
        else
            query.where(cb.and( cb.or(searchPredicates.toArray(new Predicate[0])), featuresPredicate)).distinct(true);

        if (page > 0)
            page -= 1;

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);

        List<Product> products = typedQuery.getResultList();

        //Пагинация готовой коллекции
        int listLength = products.size();

        //Начало списка
        int startIdx = Math.min(page*limit, listLength);

        //Конец списка (offset + dataOnMage)
        int endIdx = Math.min(startIdx + limit, listLength);

        //return new PageImpl<>(results, pageable, totalRows);
        return new SimpleTuple<>(listLength, products.subList(startIdx, endIdx));
    }

    //Предварительный поиск только при вводе ключевого слова
    @Override
    public List<Product> getProductsPreviewByKeyword(String key) {

        return productsRepository.findProductsByKeyword(key.toLowerCase(), PageRequest.of(0,6)).getContent();
    }

    // Полноценный полнотекстовый поиск с блэкджеком и .... (словоформами, транслитерацией) через lucene

}
