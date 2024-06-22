package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.DiscountRequestDto;
import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.products.Discount;
import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.infrastructure.enums.sorting.DiscountsSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;

import java.util.List;


public interface DiscountsService {

    // Добавление записи
    void create(Discount discount);

    Discount create(DiscountRequestDto dto);

    // Изменение записи
    void update(Discount discount);

    Discount update(DiscountRequestDto dto);


    // Деактивировать скидку
    void deactivateById(Long id, Discount discount);

    // Проверять наличие скидок (раз в n-мин) с истёкшим сроком действия и деактивировать из
    @Async
    void deactivateExpiredScheduled();

    // Активировать скидку
    void activateById(long discountId);

    // Добавить скидку в вариант товара
    ProductVariant addDiscountToPv(long discountId, long pvId);

    // Добавить скидку в список вариант товара
    void addDiscountToPvList(long discountId, List<Long> pvIdList);

    // Добавить скидку на товар
    Product addDiscountToProduct(long discountId, long productId);

    // Добавить скидку в список товаров
    void addDiscountToProductsList(long discountId, List<Long> pvIdList);

    // Добавить скидку на категорию
    void addDiscountToCategory(long discountId, long categoryId);


    // Убрать скидку у вариантов товаров заданного списка
    void removeDiscountFromPvList(long discountId, List<Long> pvIdList);

    // Убрать скидку у товаров из заданного списка
    void removeDiscountFromProductsList(long discountId, List<Long> pvIdList);

    // Добавить скидку у категорий из заданного списка
    void removeDiscountFromCategories(long discountId, List<Long> categoriesIds);

    // Выборка всех записей
    Page<Discount> getAll(int pageNum, int limit,
                          DiscountsSortEnum sortEnum, GeneralSortEnum sortType);

    // Выборка записи под id
    Discount getById(long id);

    // Выборка категорий для товаров которых задана скидка
    List<Category> getCategoriesWithDiscount(long discountId);
}
