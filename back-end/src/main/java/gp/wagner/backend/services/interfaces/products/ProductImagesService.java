package gp.wagner.backend.services.interfaces.products;

import gp.wagner.backend.domain.entites.products.ProductImage;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ProductImagesService {

    //Добавление записи
    long save(ProductImage productImage);

    long save(long productVariantId, String imgLink,int order );

    //Изменение записи
    void update(ProductImage productImage);
    void update(Long imageId, String imageLink, Integer imageOrder);

    //Удаление изображения по id
    void deleteById(long productImageId);

    //Выборка всех записей
    List<ProductImage> getAll();

    //Выборка записи по id
    ProductImage getById(Long id);

    //Выборка записи по ссылке
    ProductImage getByLink(String link);

    //Выборка записи по ссылке
    ProductImage getByVariantIdAndByOrder(long pvId, int imgOrder);

    //Выборка записи по списку id
    List<ProductImage> getByIdList(List<Long> idList);

    //Выборка записи под id варианта исполнения товара
    List<ProductImage> getByProductVariantId(Long productVariantId);


}
