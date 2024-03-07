package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeAndValRequestDto;
import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import java.util.List;

// Класс для расчётов общего кол-ва элементов при пагинации выборки
public class PaginationUtils {

    // Подсчёт количества посетителей, которые просмотрели определённые категории
    public static Long countVisitorsWithProductsViews(EntityManager entityManager) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CategoryViews> root = query.from(CategoryViews.class);
        Path<Visitor> visitorPath = root.get("visitor");

        query.select(cb.countDistinct(visitorPath.get("id")));

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества товаров для определённого производителя или категории
    public static long countProductsByProducerOrCategory(EntityManager entityManager, long requiredId, Class<?> searchingType) {

        // Проверить, по чём будет происходить выборка
        if (!searchingType.isAssignableFrom(Producer.class) || !searchingType.isAssignableFrom(Producer.class))
            return 0;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Product> root = query.from(Product.class);

        // Определить, по чём будет происходить поиск - производитель или категории
        Path<?> path = searchingType.isAssignableFrom(Producer.class) ? root.get("producer") : root.get("category");

        Predicate predicate = cb.equal(path.get("id"), requiredId);

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества товаров для определённого производителей или категорий
    public static long countProductsByProducersOrCategories(EntityManager entityManager, List<Long> requiredIds, Class<?> searchingType) {

        // Проверить, по чём будет происходить выборка
        if (!searchingType.isAssignableFrom(Producer.class) || !searchingType.isAssignableFrom(Producer.class))
            return 0;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Product> root = query.from(Product.class);

        // Определить, по чём будет происходить поиск - производитель или категории
        Path<?> path = searchingType.isAssignableFrom(Producer.class) ? root.get("producer") : root.get("category");

        Predicate predicate = path.get("id").in(requiredIds);

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества товаров для определённого производителя
    public static long countProductsByProducersOrCategoriesWithPrice(EntityManager entityManager, List<Long> requiredIds, Class<?> searchingType,
                                                                     SimpleTuple<Integer, Integer> pricesRange, ProductsOrVariantsEnum povEnum) {

        // Проверить, по чём будет происходить выборка
        if (!searchingType.isAssignableFrom(Producer.class) || !searchingType.isAssignableFrom(Producer.class))
            return 0;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Product> root = query.from(Product.class);

        // Определить, по чём будет происходить поиск - производитель или категории
        Path<?> path = searchingType.isAssignableFrom(Producer.class) ? root.get("producer") : root.get("category");

        Predicate predicate = cb.and(
                path.get("id").in(requiredIds),
                cb.equal(root.get("isDeleted"), false)/*,
                cb.isNull(root.get("productVariants").get("discount"))*/
        );

        // Сформировать ещё предикат для выборки по цене
        if (pricesRange != null && povEnum != null) {

            // Определить тип выборки по цене (базового варианта или всех вариантов товара)
            Predicate pricePredicate = povEnum == ProductsOrVariantsEnum.PRODUCTS ?
                    ServicesUtils.getProductPricePredicate(pricesRange, root, query, cb) :
                    ServicesUtils.getProductVariantPricePredicate(pricesRange, root, query, cb);

            predicate = cb.and(predicate, pricePredicate);
        }

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества заказов для определённого покупателя по id
    public static long countOrdersByCustomerEmail(EntityManager entityManager, String email, Long id) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        // Составная таблица заказов
        Root<Order> root = query.from(Order.class);

        // Присоединить таблицу покупателей
        Join<Order, Customer> orderCustomerJoin = root.join("customer");

        // Предикат для запроса
        Predicate predicate;

        if (email != null && id != null)
            predicate = cb.and(
                    cb.equal(orderCustomerJoin.get("id"), id),
                    cb.equal(orderCustomerJoin.get("email"), email)
            );
        else if (email != null)
            predicate = cb.equal(orderCustomerJoin.get("email"), email);
        else
            predicate = cb.equal(orderCustomerJoin.get("id"), id);

        query.where(predicate);

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }
}
