package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.entites.basket.Basket;
import gp.wagner.backend.domain.entites.visits.Visitor;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Date;
import java.util.List;


public interface BasketsService {

    //Добавление записи
    long create(Basket basket);
    long create(long productVariantId, int userId, int products_scout, Date addingDate);

    //Изменение записи
    void update(Basket basket);
    void update(long basketId, long productVariantId, int userId, int products_scout, Date addingDate);

    //Выборка всех записей
    List<Basket> getAll();

    //Выборка записи под id
    Basket getById(Long id);

    //Выборка записи по id пользователя
    List<Basket> getByUserId(int userId);

    //Получить все корзины по определённому варианту товара
    List<Basket> getByProductId(long productVariantId);

    //Получение максимального id - последнее добавленное значение
    long getMaxId();

    //Удалить по id пользователя и варианта товара
    long deleteBasketByUserAndProdVariant(int userId, long productId);

}
