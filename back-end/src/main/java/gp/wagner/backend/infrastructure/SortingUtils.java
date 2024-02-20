package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
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
            //default -> root.get("id");
            case PRICE -> null;
        };

        if (sortEnum == ProductsSortEnum.PRICE){

            // Ещё один вложенный подзапрос для выборки минимального id варианта для данного товара
            Subquery<Long> minIdSubquery = query.subquery(Long.class);
            Root<ProductVariant> minIdSubQueryRoot = minIdSubquery.from(ProductVariant.class);
            minIdSubquery.select(cb.min(minIdSubQueryRoot.get("id")))
                    .where(cb.equal(minIdSubQueryRoot.get("product"), root));


            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ProductVariant> subQueryRoot = subquery.from(ProductVariant.class);

            // Для получения id базового товара используется ещё один подзапрос, определённый выше
            subquery.select(subQueryRoot.get("price"))
                    .where(cb.equal(subQueryRoot.get("id"), minIdSubquery));

            expression = subquery.getSelection();
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
            default-> Sort.by("order_date");
        };

        return sortType == GeneralSortEnum.ASC ? sort.ascending() : sort.descending();
    }

    // Сформировать объект сортировки при подсчёте конверсий для товаров
    public static Sort createSortForOrdersCvrSelections(OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType){

        Sort sort = switch (sortEnum) {
            case VISITS -> Sort.by("visits");
            case AMOUNT -> Sort.by("orders_count");
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

}
