package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.PaymentMethod;
import gp.wagner.backend.domain.entites.products.Discount;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.ratings.RatingStatistics;
import gp.wagner.backend.infrastructure.enums.sorting.*;
import gp.wagner.backend.infrastructure.enums.sorting.orders.OrdersSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.orders.OrdersStatisticsSortEnum;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Sort;

public class SortingUtils {

    // Сформировать условие сортировки для товаров
    public static void createSortQueryForProducts(CriteriaBuilder cb, CriteriaQuery<?> query, Root<Product> root, ProductsSortEnum sortEnum, GeneralSortEnum sortType){


        // Сформировать запрос с агрегатной функцией подсчёта суммы
        Expression<?> expression = switch (sortEnum) {
            case ID -> root.get("id");
            case AVAILABLE -> root.get("isAvailable");
            case DISCOUNT -> null;
            case RATINGS_AMOUNT -> null;
            case PRICE -> null;
        };

        if (sortEnum == ProductsSortEnum.PRICE){

            // Ещё один вложенный подзапрос для выборки минимального id варианта для данного товара
            Subquery<Long> minVariantIdSubquery = query.subquery(Long.class);
            Root<ProductVariant> minIdSubQueryRoot = minVariantIdSubquery.from(ProductVariant.class);
            minVariantIdSubquery.select(cb.min(minIdSubQueryRoot.get("id")))
                    .where(cb.equal(minIdSubQueryRoot.get("product"), root));

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ProductVariant> subQueryRoot = subquery.from(ProductVariant.class);

            // Для получения id базового товара используется ещё один подзапрос, определённый выше
            subquery.select(subQueryRoot.get("price"))
                    .where(cb.equal(subQueryRoot.get("id"), minVariantIdSubquery));

            expression = subquery.getSelection();
        }

        // Сортировка по скидке
        if (sortEnum == ProductsSortEnum.DISCOUNT){
            Subquery<Float> discountSubQuery = query.subquery(Float.class);
            Root<Discount> discountRoot = discountSubQuery.from(Discount.class);

            Path<ProductVariant> pvPath = discountRoot.get("productVariants");

            // Найти максимальное значение скидки в вариантах товара
            discountSubQuery.select(cb.max(discountRoot.get("percentage")))
                    .where(
                            cb.equal(
                                    pvPath.get("product").get("id"),
                                    root.get("id")
                            )
                    );

            expression = discountSubQuery.getSelection();

        }

        // Сортировка по количеству оценок
        if (sortEnum == ProductsSortEnum.RATINGS_AMOUNT){
            Subquery<Long> ratingsSubquery = query.subquery(Long.class);
            Root<RatingStatistics> rsRoot = ratingsSubquery.from(RatingStatistics.class);

            Path<Product> productPath = rsRoot.get("product");

            ratingsSubquery.select(cb.max(rsRoot.get("amount")))
                    .where(
                            cb.equal(
                                    productPath.get("id"),
                                    root.get("id")
                            )
                    );

            expression = ratingsSubquery.getSelection();

        }


        query.orderBy(sortType == GeneralSortEnum.ASC ? cb.asc(expression) : cb.desc(expression));

    }

    public static Sort createSortForProducts(ProductsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort  = switch (sortEnum) {
            case ID -> Sort.by("id");
            case AVAILABLE ->  Sort.by("isAvailable");
            default -> Sort.by("id");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();

    }

    // Сформировать условие сортировки для заказов
    public static void createSortQueryForOrders(CriteriaBuilder cb, CriteriaQuery<?> query, From<?, ?> root, OrdersSortEnum sortEnum, GeneralSortEnum sortType){


        // Сформировать запрос с агрегатной функцией подсчёта суммы
        Expression<?> expression = switch (sortEnum) {
            case SUM -> root.get("sum");
            case ORDER_STATE -> root.get("orderState");
            case FULLNESS -> root.get("generalProductsAmount");
            case PAYMENT_METHOD ->  root.get("paymentMethod").get("methodName");
            default-> root.get("id");
        };


        query.orderBy(sortType == GeneralSortEnum.ASC ? cb.asc(expression) : cb.desc(expression));

    }

    // Сформировать объект сортировки заказов через PageRequest
    public static Sort createSortForOrders(OrdersSortEnum sortEnum, GeneralSortEnum sortType){

        // Сформировать запрос с агрегатной функцией подсчёта суммы
        Sort sort = switch (sortEnum) {
            case SUM -> Sort.by("sum");
            case ORDER_STATE -> Sort.by("orderState");
            case FULLNESS -> Sort.by("generalProductsAmount");
            case PAYMENT_METHOD -> Sort.by("paymentMethodId");
            default-> Sort.by("id");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();

    }

    // Сформировать объект сортировки статистики заказов через PageRequest
    public static Sort createSortForOrdersStatistics(OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case SUM -> Sort.by("orders_sum");
            case AMOUNT -> Sort.by("orders_amount");
            case VISITS -> Sort.by("visits");
            case CVR -> Sort.by("cvr");
            default-> Sort.by("order_date");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать объект сортировки при выборке заказов в определённом диапазоне дат
    public static Sort createSortForOrdersBetweenDates(OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case SUM -> Sort.by("orders_sum");
            case AMOUNT -> Sort.by("orders_count");
            default-> Sort.by("order_date_alias");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать объект сортировки при подсчёте конверсий для товаров
    public static Sort createSortForOrdersCvrSelections(OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case VISITS -> Sort.by("visits");
            case AMOUNT -> Sort.by("orders_amount");
            case CVR -> Sort.by("cvr");
            case SUM -> Sort.by("orders_sum");
            default-> Sort.by("order_date");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать объект сортировки при подсчёте конверсий для корзин
    public static Sort createSortForBasketsCvrSelections(BasketsStatisticsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case VISITS -> Sort.by("visits");
            case AMOUNT -> Sort.by("addings_amount");
            case CVR -> Sort.by("cvr");
            case SUM -> Sort.by("baskets_sum");
            default-> Sort.by("add_date");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать объект сортировки при подсчёте конверсий для корзин
    public static Sort createSortForViewsFrequencySelection(ViewsFrequencySortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case CATEGORY_ID -> Sort.by("category_id");
            case CATEGORY_NAME -> Sort.by("categ_name");
            case VIEWS_COUNT -> Sort.by("views_amount");
            case VISITOR_COUNT -> Sort.by("visitors_amount");
            default-> Sort.by("frequency");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать условие сортировки при подсчёте кол-ва заказов товаров
    public static void createSortQueryForProductsOrdersCount(CriteriaBuilder cb, CriteriaQuery<?> query, From<?, ?> source, @Nullable Expression<Long> countExpression, ProductsOrVariantsCountSortEnum sortEnum, GeneralSortEnum sortType){


        // Сформировать выражение для сортировки - по умолчанию по количеству заказов товаров или вариантов
        Expression<?> expression = switch (sortEnum) {
            case PRODUCT_ID -> source.get("id");
            case NAME -> source.get("name");
            default-> countExpression != null ? countExpression : source.get("id");
        };

        query.orderBy(sortType == GeneralSortEnum.ASC ? cb.asc(expression) : cb.desc(expression));

    }
    // Сформировать условие сортировки при подсчёте кол-ва заказов вариантов
    public static void createSortQueryForVariantsOrdersCount(CriteriaBuilder cb, CriteriaQuery<?> query, From<?, ?> source, @Nullable Expression<Long> countExpression, ProductsOrVariantsCountSortEnum sortEnum, GeneralSortEnum sortType){


        // Сформировать выражение для сортировки - по умолчанию по количеству заказов товаров или вариантов
        Expression<?> expression = switch (sortEnum) {
            case PRODUCT_ID -> source.get("id");
            case PV_ID -> source.get("id");
            case NAME -> source.get("name");
            case PV_TITLE -> source.get("title");
            default-> countExpression != null ? countExpression : source.get("id");
        };

        query.orderBy(sortType == GeneralSortEnum.ASC ? cb.asc(expression) : cb.desc(expression));

    }

    // Сформировать условие сортировки при выборке посетителей и просмотренных ими товаров
    public static void createSortQueryForVisitorsAndViews(CriteriaBuilder cb, CriteriaQuery<?> query, From<?, ?> root, Expression<Long> countExpression, Expression<Integer> sumExpression,
                                                          VisitorAndViewsSortEnum sortEnum, GeneralSortEnum sortType){


        // Сформировать выражение для сортировки - по умолчанию по id посетителя
        Expression<?> expression = switch (sortEnum) {
            case VIEWS_AMOUNT -> countExpression;
            case SUM -> sumExpression;
            default-> root.get("id");
        };

        query.orderBy(sortType == GeneralSortEnum.ASC ? cb.asc(expression) : cb.desc(expression));

    }

    // Сформировать объект сортировки при выборке скидок
    public static Sort createSortForDiscountsSelection(DiscountsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case PERCENTAGE -> Sort.by("percentage");
            case STARTS_AT -> Sort.by("startsAt");
            case ENDS_AT -> Sort.by("endsAt");
            case IS_ACTIVE -> Sort.by("isActive");
            default-> Sort.by("id");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать объект сортировки при выборке оценок товаров
    public static Sort createSortForRatingsSelection(RatingsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case RATING -> Sort.by("rating");
            case CREATED_AT -> Sort.by("createdAt");
            case UPDATED_AT -> Sort.by("updatedAt");
            default-> Sort.by("id");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать объект сортировки при выборке отзывов на товары
    public static Sort createSortForReviewsSelection(ReviewsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case PRODUCT -> Sort.by("product_id");
            case USER -> Sort.by("user_id");
            case VERIFIED -> Sort.by("isVerified");
            case CREATED_AT -> Sort.by("createdAt");
            case UPDATED_AT -> Sort.by("updatedAt");
            default-> Sort.by("id");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать объект сортировки при выборке производителей
    public static Sort createSortForProducersSelection(ProducersSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case PRODUCER_NAME -> Sort.by("producerName");
            //case DELETED_AT -> Sort.by("deletedAt");
            case IS_SHOWN -> Sort.by("isShown");
            default-> Sort.by("id");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать объект сортировки при выборке посещений магазина за определённый период
    public static Sort createSortForDailyVisits(DailyVisitsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case DATE -> Sort.by("date");
            default-> Sort.by("visits_amount");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать условие сортировки для товаров
    public static void createSortQueryForCustomers(CriteriaBuilder cb, CriteriaQuery<?> query, Root<Customer> root, CustomersSortEnum sortEnum, GeneralSortEnum sortType,
                                                   Expression<String> customersSnp, Expression<Long> ordersCountExp, Expression<Integer> ordersUnitsCountExp,
                                                   Expression<Integer> ordersSumsExp, Expression<Double> avgUnitPrice){


        // Сформировать запрос с агрегатной функцией подсчёта суммы
        Expression<?> expression = switch (sortEnum) {
            case ID -> root.get("id");
            case SNP -> customersSnp;
            case EMAIL -> root.get("email");
            case PHONE -> root.get("phoneNumber");
            case CREATED -> root.get("createdAt");
            case ORDERS_COUNT -> ordersCountExp;
            case UNITS_COUNT -> ordersUnitsCountExp;
            case AVG_UNIT_PRICE -> avgUnitPrice;
            case ORDERS_SUM -> ordersSumsExp;
        };


        query.orderBy(sortType == GeneralSortEnum.ASC ? cb.asc(expression) : cb.desc(expression));

    }
}
