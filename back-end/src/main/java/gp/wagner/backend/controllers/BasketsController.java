package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.BasketRequestDto;
import gp.wagner.backend.domain.dto.response.BasketRespDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.entites.baskets.Basket;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/baskets")
public class BasketsController {

    //Получение всех корзин для всех пользователей (только для администратора/модератора)
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<BasketRespDto> getOrderAllBaskets(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                   @Valid @RequestParam(value = "limit") @Max(80) int limit){

        Page<Basket> basketsPage = Services.basketsService.getAll(pageNum, limit);

        return new PageDto<>(
                basketsPage, () -> basketsPage.getContent().stream().map(BasketRespDto::new).toList()
        );
    }

    //Выборка корзины заданного пользователя
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BasketRespDto getBasketByUser(@PathVariable int userId){

        Basket basket = Services.basketsService.getByUserId(userId);

        if (basket == null)
            throw new ApiException(String.format("Корзина для пользователя с id: %d не найдена. Not found!", userId));

        return new BasketRespDto(basket);
    }

    //Выборка корзины по id
    @GetMapping(value = "/by_id/{basketId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BasketRespDto getBasketByUser(@PathVariable long basketId){

        Basket basket = Services.basketsService.getById(basketId);

        if (basket == null)
            throw new ApiException(String.format("Корзина с id: %d не найдена. Not found!", basketId));

        return new BasketRespDto(basket);
    }

    //Выборка корзин для определённого варианта товара
    @GetMapping(value = "/by_product_variant/{pvId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BasketRespDto> getBasketsByProductVariants(@PathVariable int pvId){

        List<Basket> basket = Services.basketsService.getByProductId(pvId);

        if (basket == null)
            throw new ApiException(String.format("Корзины для варианта товара с id: %d не найдено. Not found!", pvId));

        return basket.stream().map(BasketRespDto::new).toList();
    }

    //Добавление корзины конкретному пользователю
    @PostMapping()
    public ResponseEntity<String> createBasket(@Valid /*@RequestPart(value = "basket")*/@RequestBody BasketRequestDto basketRequestDto){

        long createdBasketId;

        try {

            createdBasketId = Services.basketsService.create(basketRequestDto);

         } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }

        return new ResponseEntity<>(String.format("Корзину c id: %d успешно создана!", createdBasketId), HttpStatusCode.valueOf(200)) ;
    }

    //Добавление ещё товаров в корзину
    @PutMapping(value = "/add_product_variants", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addProductVariants(@Valid @RequestBody(/*value = "basket"*/) BasketRequestDto basketRequestDto){

        try {

            Services.basketsService.insertProductVariants(basketRequestDto);

         } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }

        return new ResponseEntity<>(String.format("Варианты товаров добавлены в корзину c id: %d!", basketRequestDto.getId()), HttpStatusCode.valueOf(200)) ;
    }

    //Удаление записей из корзины для определённого пользователя и варианта товара
    @DeleteMapping(value = "/delete_pv")
    public ResponseEntity<String> deleteProductVariantFromBasket(@RequestParam("user_id") int userId,
                                                                 @RequestParam("product_variant_id") long productVariantId){

        long deletedId = Services.basketsService.deleteBasketByUserAndProdVariant(userId, productVariantId);

        if (deletedId > 0)
            return new ResponseEntity<>("Корзина для пользователя удалена. Basket has been deleted", HttpStatusCode.valueOf(200));
        else
            return new ResponseEntity<>(String.format("Корзину для пользователя %d и товара %d найти не удалось! Basket not found!", userId, productVariantId), HttpStatusCode.valueOf(500));

    }

}
