package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.persistence.criteria.*;

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

        //Разделить строку, чтоб получить
        String[] numbers = priceRange.split("[-–—_|]");

        //Если получить значения из строки удалось, тогда пытаемся их спарсит
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

}
