package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

//Класс для вынесения повторяющихся и вспомогательных методов из сервисов
public class ServicesUtils<T> {

    //Создание предиката для фильтрации товаров по цене
    public static Predicate getPricePredicate(String priceRange, Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        //Присоединить таблицу вариантов товаров
        Join<Product, ProductVariant> productProductVariantJoin = root.join("productVariants");

        //Создание подзапроса для получения мин.id варианта товара - базового варианта товара
        Subquery<Integer> subqueryId = query.subquery(Integer.class);
        Root<ProductVariant> subQueryRoot = subqueryId.from(ProductVariant.class);
        subqueryId.select(cb.min(subQueryRoot.get("id"))).where(cb.equal(subQueryRoot.get("product"), root));

        //Разделить строку, что бы получить отдельные токены диапазона
        String[] numbers = priceRange.split("[-–—_|]");

        //Если получить значения из строки удалось, тогда пытаемся их спарсить
        if (numbers.length > 1) {

            Integer priceLo = Utils.TryParseInt(numbers[0]);
            Integer priceHi = Utils.TryParseInt(numbers[1]);

            if (priceLo != null && priceHi != null)
                return cb.and(
                        cb.equal(productProductVariantJoin.get("id"), subqueryId),
                        cb.between(productProductVariantJoin.get("price"), priceLo, priceHi)
                );//cb.and
        }
        return null;
    }

    //Сформировать все предикаты
    public static List<Predicate> collectProductsPredicates(CriteriaBuilder cb, Root<Product> root, CriteriaQuery<?> query,
                                                            ProductFilterDtoContainer container, Long categoryId, String priceRange){
        List<Predicate> predicates = new ArrayList<>();

        //Доп.фильтрация по категории
        if (categoryId != null) {
            //Присоединить сущность категорий
            Join<Product, Category> categoryJoin = root.join("category");

            //Задать доп. условие выборки - по категориям
            predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
        }

        //Доп.фильтрация по производителям
        if (container.getProducersNames() != null && container.getProducersNames().size() > 0){
            Join<Product, Producer> producerJoin = root.join("producer");

            predicates.add(producerJoin.get("producerName").in(container.getProducersNames()));
        }

        //Доп.фильтрация по ценам
        if (priceRange != null)
            predicates.add(getPricePredicate(priceRange, root, query, cb));

        return predicates;
    }

}
