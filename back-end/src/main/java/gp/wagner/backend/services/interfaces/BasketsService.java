package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.BasketRequestDto;
import gp.wagner.backend.domain.entites.baskets.Basket;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;


public interface BasketsService {

    //Добавление записи
    long create(Basket basket);
    long create(long productVariantId, int userId, int products_scout, Date addingDate);
    long create(BasketRequestDto dto);

    // Добавить ещё товаров в корзину
    public void insertProductVariants(BasketRequestDto basketDto);

    //Изменение записи
    void update(Basket basket);
    void update(long basketId, long productVariantId, int userId, int products_scout, Date addingDate);

    // Изменить сумму при изменении стоимости варианта товара
    /**
     * <p><b>Изменить сумму цен товаров в корзине при изменении варианта товара</p></p>
     * Передаваемые параметры повлияют на количество найденных корзин.
     * То есть будет происходить поиск либо по списку id вариантов товаров либо по одному id варианта
     * @param changedPv изменённый ProductVariant
     * @return если оба параметра не заданы, тогда происходит выход из функции
     * */
    void updateBasketsOnPvPriceChanged(ProductVariant changedPv);

    /**
     * <p><b>Изменить сумму цен при скрытии варианта товара</p></p>
     * Передаваемые параметры повлияют на количество найденных корзин.
     * То есть будет происходить поиск либо по списку id вариантов товаров либо по одному id варианта
     * @param pv необязательный параметр изменённого ProductVariant
     * @param changedPvList необязательный параметр коллекции изменённых ProductVariant
     * @return если оба параметра не заданы, тогда происходит выход из функции
     * */
    void updateBasketsOnPvHidden(ProductVariant pv,List<ProductVariant> changedPvList);

    /**
     * <p><b>Пересчитать сумму цен товаров в корзине удалить вариант/варианты из корзины,если они были удалены из основной таблицы</p></p>
     * @param pv необязательный параметр изменённого ProductVariant
     * @param deletedPvList необязательный параметр коллекции изменённых ProductVariant
     * @return если оба параметра не заданы, тогда происходит выход из функции
     * */
    void updateBasketsOnPvDelete(ProductVariant pv,List<ProductVariant> deletedPvList);

    /**
     * <p>Пересчитать сумму цен товаров в корзине, если товар/товары были восстановлены</p>
     * @param pv необязательный параметр изменённого ProductVariant
     * @param disclosedPvList необязательный параметр коллекции изменённых ProductVariant
     * @return если оба параметра не заданы, тогда происходит выход из функции
     * */
    void updateBasketsOnPvDisclosure(ProductVariant pv,List<ProductVariant> disclosedPvList);

    //Выборка всех записей
    Page<Basket> getAll(int pageNumber, int dataOnPage);

    //Выборка записи под id
    Basket getById(Long id);

    //Выборка записи по id пользователя
    Basket getByUserId(long userId);

    //Получить все корзины по определённому варианту товара
    List<Basket> getByProductId(long productVariantId);

    //Получение максимального id - последнее добавленное значение
    long getMaxId();

    //Удалить по id пользователя и варианта товара
    long deleteBasketByUserAndProdVariant(long userId, long productId);



}
