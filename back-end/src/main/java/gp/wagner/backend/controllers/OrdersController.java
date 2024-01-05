package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.OrderRequestDto;
import gp.wagner.backend.domain.dto.response.BasketDto;
import gp.wagner.backend.domain.dto.response.OrderRespDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.entites.basket.Basket;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Map<String, Long> createOrder(@Valid @RequestPart(value = "order") OrderRequestDto orderRequestDto){

        SimpleTuple<Long, Long> result = Services.ordersService.create(orderRequestDto);

        Map<String, Long> idAndCode = new HashMap<>();

        idAndCode.put("id", result.getValue1());
        idAndCode.put("code", result.getValue2());

        return idAndCode;
    }

    //Получение заказа по коду
    @GetMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderRespDto getOrderByCode(@RequestParam(value = "code") long orderCode){

        Order foundOrder = Services.ordersService.getByOrderCode(orderCode);

        return new OrderRespDto(foundOrder);
    }



    //Удаление записей из корзины для определённого пользователя и варианта товара
    @DeleteMapping(value = "/{userId}/{productVariantId}")
    public String deleteBasketForUser(@PathVariable int userId, @PathVariable long productVariantId){

        long deletedId = Services.basketsService.deleteBasketByUserAndProdVariant(userId, productVariantId);

        if (deletedId > 0)
            return "Корзина для пользователя удалена. Basket has been deleted";
        else
            return String.format("Корзину для пользователя %d и товара %d найти не удалось! Basket not found!", userId, productVariantId);

    }

}
