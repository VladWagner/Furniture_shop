package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.BasketRequestDto;
import gp.wagner.backend.domain.dto.response.BasketRespDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.entities.baskets.Basket;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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

    // Выборка корзины заданного пользователя
    @GetMapping(value = "/user/{user_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BasketRespDto getBasketByUser(@PathVariable(value = "user_id") int userId){

        Basket basket = Services.basketsService.getByUserId(userId);

        if (basket == null)
            throw new ApiException(String.format("Корзина для пользователя с id: %d не найдена. Not found!", userId));

        return new BasketRespDto(basket);
    }

    // Выборка корзины аутентифицированного и авторизированного пользователя
    @GetMapping(value = "/get_for_user", produces = MediaType.APPLICATION_JSON_VALUE)
    public BasketRespDto getBasketForUser(){

        Basket basket = Services.basketsService.getForAuthenticatedUser();

        if (basket == null)
            throw new ApiException("Корзина для пользователя не найдена. Not found!");

        return new BasketRespDto(basket);
    }

    //Выборка корзины по id
    @GetMapping(value = "/by_id/{basket_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BasketRespDto getBasketById(@PathVariable(value = "basket_id") long basketId){

        Basket basket = Services.basketsService.getById(basketId);

        if (basket == null)
            throw new ApiException(String.format("Корзина с id: %d не найдена. Not found!", basketId));

        return new BasketRespDto(basket);
    }

    //Выборка корзин для определённого варианта товара
    @GetMapping(value = "/by_product_variant/{pv_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BasketRespDto> getBasketsByProductVariants(@PathVariable(value = "pv_id") int pvId){

        List<Basket> basket = Services.basketsService.getByProductId(pvId);

        if (basket == null)
            throw new ApiException(String.format("Корзины для варианта товара с id: %d не найдено. Not found!", pvId));

        return basket.stream().map(BasketRespDto::new).toList();
    }

    //Добавление корзины конкретному пользователю
    @PostMapping()
    public ResponseEntity<BasketRespDto> createBasket(@Valid @RequestBody BasketRequestDto basketRequestDto){

        Basket createdBasket = Services.basketsService.create(basketRequestDto);

        if (createdBasket == null)
            throw new ApiException("Не удалось создать корзину!");

        //return new ResponseEntity<>(String.format("Корзина c id: %d успешно создана!", createdBasketId), HttpStatusCode.valueOf(200)) ;
        return ResponseEntity.ok(new BasketRespDto(createdBasket)) ;
    }

    // Обновление корзины конкретному пользователю
    @PutMapping("/update")
    public ResponseEntity<?> updateBasket(@Valid @RequestBody BasketRequestDto basketRequestDto,
                                                      @RequestParam(value = "return_basket", required = false, defaultValue = "true") boolean returnBasket){

        Basket createdBasket = Services.basketsService.updateOrCreate(basketRequestDto);

        if (createdBasket == null)
            throw new ApiException("Не удалось создать корзину!");

        return returnBasket ? ResponseEntity.ok(new BasketRespDto(createdBasket)) : ResponseEntity.ok("Basket successfully updatedd");
    }

    //Добавление ещё товаров в корзину
    @PutMapping(value = "/add_product_variants", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BasketRespDto> addProductVariants(@Valid @RequestBody() BasketRequestDto basketRequestDto){

        // Изменить логику работы с пользователем - получать пользователя из SecurityContext
        Basket updatedBasket = Services.basketsService.insertProductVariants(basketRequestDto);

        if (updatedBasket == null)
            throw new ApiException("Не удалось добавить варианты корзину!");


        //return new ResponseEntity<>(String.format("Варианты товаров добавлены в корзину c id: %d!", basketRequestDto.getId()), HttpStatusCode.valueOf(200)) ;
        return ResponseEntity.ok(new BasketRespDto(updatedBasket)) ;
    }

    //Удаление записей из корзины для определённого пользователя и варианта товара (только админ/модератор)
    @DeleteMapping(value = "/delete_pv_admin")
    public ResponseEntity<String> deleteProductVariantFromBasketAdmin(@RequestParam("user_id") int userId,
                                                                 @RequestParam("product_variant_id") long productVariantId){

        long deletedId = Services.basketsService.deleteBasketByUserAndProdVariant(userId, productVariantId);

        if (deletedId > 0)
            return new ResponseEntity<>("Корзина для пользователя удалена. Basket has been deleted", HttpStatusCode.valueOf(200));
        else
            return new ResponseEntity<>(String.format("Корзину для пользователя %d и товара %d найти не удалось! Basket not found!", userId, productVariantId), HttpStatusCode.valueOf(500));

    }

    // Удаление записей из корзины для определённого пользователя и варианта товара. Пользователь определяется по access_token
    @DeleteMapping(value = "/delete_pv")
    public ResponseEntity<BasketRespDto> deleteProductVariantFromBasket(@RequestParam("product_variant_id") long productVariantId){

        Basket changedBasket = Services.basketsService.deleteBasketByAuthUserAndProdVariant(productVariantId);


        if (changedBasket == null)
            throw new ApiException(String.format("Удалить вариант товара с id: %d не удалось!", productVariantId));

        return ResponseEntity.ok(new BasketRespDto(changedBasket));
    }


    // Изменить кол-во единиц определённого варианта товара
    @PutMapping(value = "/change_counter", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BasketRespDto> updateProductVariantCounter(@RequestParam("product_variant_id") long pvId,
                                                           @RequestParam("count") int count){

        Basket basket = Services.basketsService.updateProductVariantCounter(pvId, count);

        if (basket == null)
            throw new ApiException(String.format("Изменить счётчик в корзине для варианта товара с id: %d не удалось!", pvId));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new BasketRespDto(basket));
    }

}
