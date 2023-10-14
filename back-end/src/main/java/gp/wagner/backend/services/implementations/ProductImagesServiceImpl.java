package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.entites.products.ProductImage;
import gp.wagner.backend.repositories.ProductImagesRepository;
import gp.wagner.backend.services.interfaces.ProductImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductImagesServiceImpl implements ProductImagesService {

    //Репозиторий
    private ProductImagesRepository repository;

    @Autowired
    public void setProductImagesRepository(ProductImagesRepository productImagesRepository) {
        this.repository = productImagesRepository;
    }

    @Override
    public long save(ProductImage productImage) {
        if (productImage == null)
            return -1;

        repository.saveAndFlush(productImage);

        return repository.getMaxId();
    }

    @Override
    public long save(long productVariantId, String imgLink, int order) {
        if (productVariantId <= 0)
            return -1;

        repository.insertProductImage(productVariantId, imgLink, order);

        return repository.getMaxId();
    }

    @Override
    public void update(ProductImage productImage) {
        if (productImage == null)
            return;

        //Мгновенная запись изменений в БД
        repository.saveAndFlush(productImage);
    }

    //Изменение изображения товара по id
    @Override
    public void update(Long imageId, String imageLink, Integer imageOrder) {
        repository.updateProductImage(imageId, imageLink, imageOrder);
    }

    @Override
    public void deleteById(long productImageId) {
        repository.deleteById(productImageId);
    }

    @Override
    public List<ProductImage> getAll() {
        return repository.findAll();
    }

    @Override
    public ProductImage getById(Long id) {

        return  repository.findById(id).get();
    }

    @Override
    public ProductImage getByLink(String link) {
        return repository.findProductImageByImgLinkEquals(link).orElse(null);
    }

    @Override
    public ProductImage getByVariantIdAndByOrder(long pvId, int imgOrder) {
        return repository.findByVariantIdAndImgOrder(pvId, imgOrder).orElse(null);
    }

    @Override
    public List<ProductImage> getByIdList(List<Long> idList) {

        if (idList == null || idList.size() == 0)
            return null;

        return repository.findProductImagesByIdIn(idList);
    }

    @Override
    public List<ProductImage> getByProductVariantId(Long productVariantId) {
        return repository.findProductImagesByProductVariantId(productVariantId);
    }
}
