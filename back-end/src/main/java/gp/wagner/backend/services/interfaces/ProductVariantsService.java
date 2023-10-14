package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.ProductVariantDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ProductVariantsService {

    //Добавление записи
    long save(ProductVariant productVariant);

    long save(long productId, String previewImg,String title, int price );

    long create(ProductVariantDto dto, String previewImg);

    //Изменение записи
    void update(ProductVariant ProductVariant);
    ProductVariant update(ProductVariantDto productVariantDto, String previewImg);
    void update(long productVariantId, long productId, String previewImg,String title, int price );

    //Изменить изображение предосмотра
    void updatePreview(long productVariantId, String previewImg);

    //Получить максимальный id
    long getMaxId();

    //Выборка всех записей
    List<ProductVariant> getAll();

    //Выборка записи под id
    ProductVariant getById(Long id);

    //Выборка записи под id товара
    List<ProductVariant> getByProductId(Long productId);

}
