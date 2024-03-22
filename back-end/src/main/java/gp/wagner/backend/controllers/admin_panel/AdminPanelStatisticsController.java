package gp.wagner.backend.controllers.admin_panel;

import gp.wagner.backend.domain.dto.request.admin_panel.CustomerStatRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeAndValRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.OrdersAndBasketsCountFiltersRequestDto;
import gp.wagner.backend.domain.dto.request.crud.CustomerRequestDto;
import gp.wagner.backend.domain.dto.request.filters.OrderReportDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.admin_panel.*;
import gp.wagner.backend.domain.dto.response.category_views.VisitorAndCategoryViewsRespDto;
import gp.wagner.backend.domain.dto.response.product_views.ProductViewRespDto;
import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.exporters.implementations.ProductsVariantsOrdersCount.ProductsVariantsOrdersCountXlsExporter;
import gp.wagner.backend.exporters.implementations.TopProductsVariantsInBaskets.TopProductsVariantsInBasketsXlsExporter;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import gp.wagner.backend.infrastructure.enums.sorting.*;
import gp.wagner.backend.infrastructure.enums.sorting.orders.OrdersStatisticsSortEnum;
import gp.wagner.backend.middleware.Services;
import jakarta.persistence.Tuple;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/admin_panel/stat")
public class AdminPanelStatisticsController {

    // Получить статистику просмотров по дням за определённый период
    @GetMapping(value = "/get_daily_visits", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DailyVisitsRespDto> getDailyVisitsCount(
            @Valid @RequestPart(value = "dates_range") DatesRangeRequestDto datesRangeDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "date")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) throws ApiException {

        Page<SimpleTuple<Date, Long>> dailyViewsPage = Services.adminPanelStatisticsService.getDailyVisitsByDatesRange(datesRangeDto, pageNum, limit,
                DailyVisitsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(dailyViewsPage, () -> dailyViewsPage.getContent().stream().map(DailyVisitsRespDto::new).toList());
    }

    // Получить сумму посещений интернет-магазина за определённый период
    @GetMapping(value = "/get_daily_visits_sum", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> getDailyVisitsSum(
            @Valid @RequestBody DatesRangeRequestDto datesRangeDto)  {

        long dailyVisitsSum = Services.adminPanelStatisticsService.getVisitSum(datesRangeDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(dailyVisitsSum);
    }

    // Получить заказы по дням их суммы. В определённом периоде
    @GetMapping(value = "/get_daily_orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DailyOrdersRespDto> getOrdersByDaysBetweenDates(
            @Valid @RequestBody DatesRangeAndValRequestDto datesRangeDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "date")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType)  {

        // В DatesRangeAndValRequestDto задано Long значение - требуемый стастус заказа
        Page<Object[]> dailyOrdersStat = Services.adminPanelStatisticsService.getOrdersDatesRange(datesRangeDto, pageNum, limit,
                OrdersStatisticsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(dailyOrdersStat, () -> dailyOrdersStat.getContent().stream().map(DailyOrdersRespDto::new).toList());
    }

    // Получить статистику заказов по дням их суммы. В определённом периоде
    @GetMapping(value = "/get_daily_orders_statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DailyOrdersStatisticsRespDto> getOrdersStatisticsByDaysBetweenDates(
            @Valid @RequestBody OrderReportDto datesRangeDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "date")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType)  {


        Page<Tuple> dailyOrdersStat = Services.ordersService.getDailyOrdersStatistics(datesRangeDto, pageNum, limit,
                OrdersStatisticsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(dailyOrdersStat, () -> dailyOrdersStat.getContent().stream().map(DailyOrdersStatisticsRespDto::new).toList());
    }

    // Подсчёт конверсий по дням из просмотра в заказ в определённой категории
    @GetMapping(value = "/get_daily_cvr_category", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DailyConversionsRespDto> getOrdersCvrBetweenDatesInCategory(
            @Valid @RequestBody DatesRangeAndValRequestDto datesRangeDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "date")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType)  {

        Page<Object[]> dailyCvr = Services.adminPanelStatisticsService.getConversionFromViewToOrderInCategory(datesRangeDto, pageNum, limit,
                OrdersStatisticsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(dailyCvr, () -> dailyCvr.getContent().stream().map(DailyConversionsRespDto::new).toList());
    }

    @GetMapping(value = "/get_daily_cvr_category_criteria", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DailyConversionsRespDto> getOrdersCvrBetweenDatesInCategoryCriteria(
            @Valid @RequestBody DatesRangeAndValRequestDto datesRangeDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "date")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType)  {

        Page<Tuple> dailyCvr = Services.adminPanelStatisticsService.getConversionFromViewToOrderInCategoryCriteria(datesRangeDto, pageNum, limit,
                OrdersStatisticsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(dailyCvr, () -> dailyCvr.getContent().stream().map(DailyConversionsRespDto::new).toList());
    }

    // Подсчёт конверсий по дням из просмотра в заказ в для определённого товара
    @GetMapping(value = "/get_daily_cvr_product", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DailyConversionsRespDto> getOrdersCvrBetweenDatesForProduct(
            @Valid @RequestBody DatesRangeAndValRequestDto datesRangeDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "date")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType)  {

        Page<Object[]> dailyCvr = Services.adminPanelStatisticsService.getConversionFromViewToOrderForProduct(datesRangeDto, pageNum, limit,
                OrdersStatisticsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(dailyCvr, () -> dailyCvr.getContent().stream().map(DailyConversionsRespDto::new).toList());
    }

    // Подсчёт конверсий по дням из просмотра в добавление в корзину для определённого товара
    @GetMapping(value = "/daily_basket_cvr_product", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DailyConversionsRespDto> getBasketsCvrBetweenDatesForProduct(
            @Valid @RequestBody DatesRangeAndValRequestDto datesRangeDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "date")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Object[]> dailyCvr = Services.adminPanelStatisticsService.getConversionFromViewToBasket(datesRangeDto, pageNum, limit,
                BasketsStatisticsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(dailyCvr, () -> dailyCvr.getContent().stream().map(DailyConversionsRespDto::new).toList());
    }

    // Максимальная, средняя и минимальная конверсия из просмотра в заказ в категории
    @GetMapping(value = "/get_min_avg_max_cvr_category", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuantityValuesRespDto> getQuantityValuesOrdersInCategory(
            @Valid @RequestBody DatesRangeAndValRequestDto datesRangeDto)  {

        Object[] result = Services.adminPanelStatisticsService.getCvrValuesForOrdersInCategory(datesRangeDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new QuantityValuesRespDto(result));
    }

    // Максимальная, средняя и минимальная конверсия из просмотра в заказ для товара
    @GetMapping(value = "/get_min_avg_max_cvr_product", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuantityValuesRespDto> getQuantityValuesOrdersForProduct(
            @Valid @RequestBody DatesRangeAndValRequestDto datesRangeDto)  {

        Object[] result = Services.adminPanelStatisticsService.getCvrValuesForOrdersForProduct(datesRangeDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new QuantityValuesRespDto(result));
    }

    // Частота просмотров товаров в категории на посетителя
    @GetMapping(value = "/products_views_frequency", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ViewsFrequencyRespDto> getProductsViewsFrequency(
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "date")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Object[]> result = Services.adminPanelStatisticsService.getProductsViewsFrequency(pageNum, limit,
                ViewsFrequencySortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(result, () -> result.getContent().stream().map(ViewsFrequencyRespDto::new).toList());
    }

    // Частота просмотров категорий на посетителя
    @GetMapping(value = "/categories_views_frequency", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ViewsFrequencyRespDto> getCategoriesViewsFrequency(
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "date")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Object[]> result = Services.adminPanelStatisticsService.getCategoriesViewsFrequency(pageNum, limit,
                ViewsFrequencySortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(result, () -> result.getContent().stream().map(ViewsFrequencyRespDto::new).toList());
    }

    // Количество заказов каждого товара в категории + фильтр
    @GetMapping(value = "/products_orders_count", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProductsOrdersCountRespDto> getProductsOrdersCount(
            @Valid @RequestBody OrdersAndBasketsCountFiltersRequestDto filtersRequestDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Tuple> resultPage = Services.adminPanelStatisticsService.getOrdersCountForEachProduct(filtersRequestDto, pageNum, limit, ProductsOrVariantsEnum.PRODUCTS,
                ProductsOrVariantsCountSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(resultPage, () -> resultPage.getContent().stream().map(ProductsOrdersCountRespDto::new).toList());
    }

    // Количество заказов каждого варианта товара в категории + фильтр
    @GetMapping(value = "/products_variants_orders_count", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProductsVariantsOrdersCountRespDto> getProductsVariantsOrdersCount(
            @Valid @RequestBody OrdersAndBasketsCountFiltersRequestDto filtersRequestDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Tuple> resultPage = Services.adminPanelStatisticsService.getOrdersCountForEachProduct(filtersRequestDto, pageNum, limit, ProductsOrVariantsEnum.VARIANTS,
                ProductsOrVariantsCountSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(resultPage, () -> resultPage.getContent().stream().map(ProductsVariantsOrdersCountRespDto::new).toList());
    }

    // Топ вариантов товаров по добавлениям в корзины
    @GetMapping(value = "/top_products_variants_baskets_count", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<TopProductsVariantsInBasketsRespDto> getTopProductsInBaskets(
            @Valid @RequestBody OrdersAndBasketsCountFiltersRequestDto filtersRequestDto,
            @RequestParam(value = "percentage", defaultValue = "0.2") float percentage,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Tuple> resultPage = Services.adminPanelStatisticsService.getTopProductsInBasket(filtersRequestDto, pageNum, limit, percentage,
                ProductsOrVariantsCountSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(resultPage, () -> resultPage.getContent().stream().map(TopProductsVariantsInBasketsRespDto::new).toList());
    }

    // Возврат CSV файла с количеством заказов каждого товара в категории + фильтр
    @GetMapping(value = "/products_variants_orders_count_xls")
    public ResponseEntity<Resource> getProductsVariantsOrdersCountXlsx(
            @Valid @RequestBody OrdersAndBasketsCountFiltersRequestDto filtersRequestDto) {

        List<Tuple> tupleList = Services.adminPanelStatisticsService.getOrdersCountForEachProduct(filtersRequestDto, ProductsOrVariantsEnum.VARIANTS);
        List<ProductsVariantsOrdersCountRespDto> dtoList = tupleList.stream().map(ProductsVariantsOrdersCountRespDto::new).toList();

        ProductsVariantsOrdersCountXlsExporter exporter = new ProductsVariantsOrdersCountXlsExporter(dtoList,"Orders of products variants");

        exporter.createHeaderLine()
                .writeTableRows();

        String fileName = String.format("Products_variants_orders_count_%s.xlsx", Utils.sdf_date_only.format(new Date()));
        Resource file = exporter.export();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    // Возврат XLS файла с топом вариантов товаров по добавлениям в корзины
    @GetMapping(value = "/top_products_variants_baskets_count_xls")
    public ResponseEntity<Resource> getTopProductsInBasketsXlsx(
            @Valid @RequestBody OrdersAndBasketsCountFiltersRequestDto filtersRequestDto,
            @RequestParam(value = "percentage", defaultValue = "0.2") float percentage) {

        List<Tuple> tupleList = Services.adminPanelStatisticsService.getTopProductsInBasket(filtersRequestDto, percentage);
        List<TopProductsVariantsInBasketsRespDto> dtoList = tupleList.stream().map(TopProductsVariantsInBasketsRespDto::new).toList();

        TopProductsVariantsInBasketsXlsExporter exporter = new TopProductsVariantsInBasketsXlsExporter(dtoList,"Baskets with products variants");

        exporter.createHeaderLine()
                .writeTableRows();

        String fileName = String.format("Products_variants_baskets_top_count_%s.xlsx", Utils.sdf_date_only.format(new Date()));
        Resource file = exporter.export();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    // Так же стоит добавить сохранение в CSV, как это делать показано здесь: https://springhow.com/spring-boot-export-to-csv/#google_vignette

    // Выборка определённых просмотренных товаров покупателем
    @GetMapping(value = "/viewed_products_by_customer", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProductViewRespDto> getCustomerViewedProducts(
            @Valid @RequestBody CustomerStatRequestDto customerDto,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit) {

        Page<ProductViews> resultPage = Services.adminPanelStatisticsService.getProductsViewsForCustomer(customerDto, pageNum, limit);

        return new PageDto<>(resultPage, () -> resultPage.getContent().stream().map(ProductViewRespDto::new).toList());
    }

    // Выборка посетителей и просмотренных ими товаров
    @GetMapping(value = "/visitors_categories_views_", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<VisitorAndCategoryViewsRespDto> getAllVisitorsCategoriesViews(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                                                 @Valid @RequestParam(value = "limit") @Max(80) int limit,
                                                                                 @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
                                                                                 @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Tuple> categoriesViewsPaged = Services.categoryViewsService.getVisitorsAndCategoriesViews(pageNum, limit,
                VisitorAndViewsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));


        Map<Long,Visitor> visitors = Services.visitorsService.getByIdList(categoriesViewsPaged.getContent()
                .stream()
                .map(t -> t.get(0, Long.class))
                .toList()
        ).stream().collect(Collectors.toMap(
                Visitor::getId,
                v -> v));

        return new PageDto<>(categoriesViewsPaged, () -> categoriesViewsPaged.getContent().stream()
                .map(t -> new VisitorAndCategoryViewsRespDto(t, visitors))
                .toList()
        );
    }

}
