package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.OrderRequestDto;
import gp.wagner.backend.domain.dto.response.orders.OrderAndPvRespDto;
import gp.wagner.backend.domain.dto.response.orders.OrderRespDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/orders")
public class OrdersController {

    //Получение всех заказа
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<OrderRespDto> getOrderAllOrders(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                @Valid @RequestParam(value = "limit") @Max(80) int limit){

        Page<Order> orderPage = Services.ordersService.getAll(pageNum, limit);

        return new PageDto<>(
                orderPage, () -> orderPage.getContent().stream().map(OrderRespDto::new).toList()
        );
    }

    //Добавление заказа
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> createOrder(@Valid /*@RequestPart(value = "order")*/ @RequestBody OrderRequestDto orderRequestDto){

        SimpleTuple<Long, Long> result = Services.ordersService.create(orderRequestDto);

        Map<String, Long> idAndCode = new HashMap<>();

        idAndCode.put("id", result.getValue1());
        idAndCode.put("code", result.getValue2());

        return idAndCode;
    }

    //Добавление ещё товаров в заказ/изменение кол-ва товаров в заказе
    @PutMapping(value = "/add_product_variants", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addProductVariants(@Valid @RequestBody() OrderRequestDto orderRequestDto){

        try {

            Services.ordersService.insertProductVariants(orderRequestDto);

        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }

        return new ResponseEntity<>(String.format("Вариант товаров добавлены в заказ '%d'!", orderRequestDto.getCode()), HttpStatusCode.valueOf(200)) ;
    }

    //Получение заказа по коду
    @GetMapping(value = "/order_by_code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderRespDto getOrderByCode(@PathVariable long code){

        // Если вариант товара будет show == false, тогда на фронте нужно будет отображать эту информацию и упоминать, что сумма пересчитана
        Order foundOrder = Services.ordersService.getByOrderCode(code);

        // Если вариант будет скрыт, тогда не вводи цену, но выводи кол-во и сделай блок немного тусклым и
        // в tooltip пиши, что сумма пересчитана

        return new OrderRespDto(foundOrder);
    }

    //Получение заказов по id варианта товара
    @GetMapping(value = "/orders_by_pv", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderAndPvRespDto> getOrderByProductVariant(@RequestParam(value = "product_variant_id") long productVariantId){

        // Если вариант товара будет show == false, тогда на фронте нужно будет отображать эту информацию и упоминать, что сумма пересчитана
        List<OrderAndProductVariant> foundOpvList = Services.ordersService.getOrdersByProductVariant(productVariantId);

        if (foundOpvList.isEmpty())
            throw new ApiException(String.format("Не удалось найти заказы для варианта товара с id: %d", productVariantId));

        return foundOpvList.stream().map(OrderAndPvRespDto::new).toList();
    }

    //Удаление вариантов товара в определённом заказе
    @DeleteMapping(value = "/delete_by_code")
    public ResponseEntity<String> deleteProductVariantByCode(@RequestParam(value = "code") long code, @RequestParam(value = "pv_id") long productVariantId){

        boolean isDeleted = Services.ordersService.deletePVFromOrder(code, productVariantId);

        if (isDeleted)
            return new ResponseEntity<>(String.format("Вариант товара с id %d успешно удалён!", productVariantId), HttpStatusCode.valueOf(200));
        else
            return new ResponseEntity<>(String.format("Не получилось удалить вариант товара. Скорее всего не найдена запись для варианта с id %d\nЛибо заказ уже в обрабтке!",
                    productVariantId), HttpStatusCode.valueOf(500));

    }

}
