package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.BasketRequestDto;
import gp.wagner.backend.domain.dto.response.BasketDto;
import gp.wagner.backend.domain.dto.response.CategoryDto;
import gp.wagner.backend.domain.entites.basket.Basket;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping(value = "/api/baskets")
public class BasketsController {

    //Выборка всех корзин заданного пользователя
    @GetMapping(value = "/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BasketDto> getBasketByUser(@PathVariable int id){

        List<Basket> baskets = Services.basketsService.getByUserId(id);

        if (baskets == null)
            throw new ApiException(String.format("Корзина для пользователя с id: %d не найдена. Not found!", id));

        return  baskets.stream().map(b -> new BasketDto(b,
                //Services.productVariantsService.getById(b.getProductVariant().getId()),
                b.getProductVariant(),
                b.getProductVariant().getProduct())
                )
                .toList();
    }

    //Добавление корзины конкретному пользователю
    @PostMapping()
    public String createBasket(@Valid @RequestPart(value = "basket") BasketRequestDto basketRequestDto){

        long createdBasketId = 0;

        try {

            createdBasketId = Services.basketsService.create(basketRequestDto.getProductVariantId(),
                    basketRequestDto.getUserId().intValue(),
                    basketRequestDto.getProductsAmount(),
                    basketRequestDto.getAddedDate());

         } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }

        return String.format("Вариант товара с id: %d добавлен в корзину c id: %d!", basketRequestDto.getProductVariantId(), createdBasketId);
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
