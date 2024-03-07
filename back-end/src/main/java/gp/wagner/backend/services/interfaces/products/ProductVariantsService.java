package gp.wagner.backend.services.interfaces.products;

import gp.wagner.backend.domain.dto.request.crud.ProductVariantDto;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;

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

    // Изменить показ варианта товара
    void updatePvDisplay(long productVariantId);

    // Изменить показ варианта товара на основании флага товара
    void updateCascadePvDisplay(Product product);

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

    // Удалить по id варианта
    boolean deleteById(long pvId);

    // Восстановить из удаления по id варианта
    boolean recoverDeletedById(long pvId);

    // Удалить варианты по id товара
    void deleteByProductId(long productId);

    // Восстановить из удаления варианты по id товара
    void recoverDeletedByProductId(long productId);

    // Удалить варианты по списку товаров
    void deleteByProductIdList(List<Long> productsIds);

    // Восстановить варианты по списку товаров
    void recoverDeletedByProductIdList(List<Long> productsIds);

    void hideByProductsList(List<Product> products);
    void hideByProductId(Product product);

    void recoverHiddenByProductsList(List<Product> products);
    void recoverHiddenByProductId(Product product);

    void deleteOrRecoverVariant(Long id, List<Long> idList, boolean deletionFlag);

    List<ProductVariant> getByProductsIds(List<Long> productsIdsList);
}
