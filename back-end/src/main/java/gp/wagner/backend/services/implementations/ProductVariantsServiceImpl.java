package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.ProductVariantDto;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.CategoriesRepository;
import gp.wagner.backend.repositories.ProductVariantsRepository;
import gp.wagner.backend.services.interfaces.ProductVariantsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductVariantsServiceImpl implements ProductVariantsService {

    //Репозиторий
    private ProductVariantsRepository productVariantsRepository;

    @Autowired
    public void setProductVariantsRepository(ProductVariantsRepository productVariantsRepository) {
        this.productVariantsRepository = productVariantsRepository;
    }


    //region Добавление
    @Override
    public long save(ProductVariant productVariant) {
        if (productVariant == null)
            return -1;

        return productVariantsRepository.saveAndFlush(productVariant).getId();
    }

    @Override
    public long save(long productId, String previewImg, String title, int price) {
        if (productId <= 0)
            return -1;

        productVariantsRepository.insertProductVariant(productId,previewImg, title, price);

        return productVariantsRepository.getMaxId();

    }

    @Override
    public long create(ProductVariantDto dto, String previewImg) {

        if (dto == null)
            return -1;

        ProductVariant pv = new ProductVariant(dto.getTitle(),
                Services.productsService.getById(dto.getProductId()),
                dto.getPrice(),
                previewImg.isEmpty() ? Constants.EMPTY_IMAGE.toString() : previewImg,
                dto.getShowProductVariant() != null ? dto.getShowProductVariant() : true);

        return productVariantsRepository.saveAndFlush(pv).getId();
    }
    //endregion


    //region Изменение
    @Override
    public void update(ProductVariant productVariant) {
        if (productVariant == null)
            return;
        productVariantsRepository.saveAndFlush(productVariant);

    }

    @Override
    public ProductVariant update(ProductVariantDto dto, String previewImg) {
        if (dto == null)
            return null ;

        if (previewImg == null || previewImg.isEmpty())
            previewImg = productVariantsRepository.findById(dto.getId()).get().getPreviewImg();

        productVariantsRepository.updateProductVariant(dto.getProductId(), dto.getProductId(), previewImg, dto.getTitle(), dto.getPrice());

        ProductVariant pv = new ProductVariant(dto.getTitle(),
                Services.productsService.getById(dto.getProductId()),
                dto.getPrice(),
                previewImg.isEmpty() ? Constants.EMPTY_IMAGE.toString() : previewImg,
                dto.getShowProductVariant() != null ? dto.getShowProductVariant() : true);

        pv.setId(dto.getId());

        return productVariantsRepository.saveAndFlush(pv);

    }

    @Override
    public void update(long productVariantId, long productId, String previewImg, String title, int price) {
        productVariantsRepository.updateProductVariant(productVariantId, productId, previewImg, title, price);
    }
    //endregion

    //Изменить изображение предосмотра
    @Override
    public void updatePreview(long productVariantId, String previewImg) {
        productVariantsRepository.updateProductVariantPreview(productVariantId, previewImg);
    }

    @Override
    public long getMaxId() {
        return productVariantsRepository.getMaxId();
    }

    @Override
    public List<ProductVariant> getAll() {
        return productVariantsRepository.findAll();
    }

    @Override
    public ProductVariant getById(Long id) {
        return productVariantsRepository.findById(id).orElse(null);
    }

    @Override
    public List<ProductVariant> getByProductId(Long productId) {
        return productVariantsRepository.findProductVariantsByProductId(productId);
    }
}
