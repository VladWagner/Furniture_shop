package gp.wagner.backend.services.interfaces.admin_panels;

import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeAndValRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.OrdersAndBasketsCountFiltersRequestDto;
import gp.wagner.backend.domain.dto.request.crud.CustomerRequestDto;
import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.enums.StatisticsObjectEnum;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;


public interface AdminPanelStatisticsService {

    // Выборка посещение интернет-магазина по дням за определённый период
    Page<SimpleTuple<Date, Long>> getDailyVisitsByDatesRange(DatesRangeRequestDto datesDto, int pageNum, int dataOnPage) throws ApiException;

    // Выборка сумму посещений за определённый период
    long getVisitSum(DatesRangeRequestDto datesDto);

    // Выборка кол-ва заказов по дням за определённый период
    Page<Object[]> getOrdersDatesRange(DatesRangeAndValRequestDto datesDto, int pageNum, int dataOnPage);

    // Подсчёт конверсии из просмотра в заказ в определённой категории
    Page<Object[]> getConversionFromViewToOrderInCategory(DatesRangeAndValRequestDto datesDto, int pageNum, int dataOnPage);

    // Подсчёт конверсии из просмотра в заказ в определённого товара
    Page<Object[]> getConversionFromViewToOrderForProduct(DatesRangeAndValRequestDto datesDto, int pageNum, int dataOnPage);

    // Подсчёт конверсии из просмотра в добавление в корзину определённого товара
    Page<Object[]> getConversionFromViewToBasket(DatesRangeAndValRequestDto datesDto, int pageNum, int dataOnPage);

    // Максимальная, средняя и минимальная конверсия за период в категории
    Object[] getCvrValuesForOrdersInCategory(DatesRangeAndValRequestDto datesDto);

    // Максимальная, средняя и минимальная конверсия за период для определённого товара
    Object[] getCvrValuesForOrdersForProduct(DatesRangeAndValRequestDto datesDto);

    // Частота просмотров товаров по категориям на посетителя
    Page<Object[]> getProductsViewsFrequency(int pageNum, int dataOnPage);

    // Частота просмотров самих категорий на посетителя
    Page<Object[]> getCategoriesViewsFrequency(int pageNum, int dataOnPage);

    // Количество заказов каждого товара/варианта + фильтр
    Page<Tuple> getOrdersCountForEachProduct(OrdersAndBasketsCountFiltersRequestDto filtersDto, int pageNum, int dataOnPage, StatisticsObjectEnum statisticsEnum);

    // Количество заказов каждого товара/варианта без пагинации - для формирования CSV/XLS
    List<Tuple> getOrdersCountForEachProduct(OrdersAndBasketsCountFiltersRequestDto filtersDto, StatisticsObjectEnum statisticsEnum);


    // Топ товаров по добавлениям в корзины
    /**
     * Выборка топ n-% товаров по добавлениям в корзину (20-30-40% самых добавляемых товаров в корзину)
     * @param percentage какой % наилучших товаров выбирать
     */
    Page<Tuple> getTopProductsInBasket(OrdersAndBasketsCountFiltersRequestDto filtersDto, int pageNum, int dataOnPage, float percentage);
    List<Tuple> getTopProductsInBasket(OrdersAndBasketsCountFiltersRequestDto filtersDto, float percentage);

    // Товары, просмотренные покупателем
    Page<ProductViews> getProductsViewsForCustomer(CustomerRequestDto customerDto, int pageNum, int dataOnPage);
}
