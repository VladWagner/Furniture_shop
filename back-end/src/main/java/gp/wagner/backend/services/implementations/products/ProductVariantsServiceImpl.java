package gp.wagner.backend.services.implementations.products;

import gp.wagner.backend.domain.dto.request.crud.ProductVariantDto;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.products.ProductVariantsRepository;
import gp.wagner.backend.services.interfaces.products.ProductVariantsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductVariantsServiceImpl implements ProductVariantsService {

    @PersistenceContext
    private EntityManager entityManager;


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

        ProductVariant pv = new ProductVariant(null ,dto.getTitle(),
                Services.productsService.getById(dto.getProductId()),
                dto.getPrice(),
                previewImg.isEmpty() ? Constants.EMPTY_IMAGE.toString() : previewImg,
                dto.getShowPv() != null ? dto.getShowPv() : true, null);

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

        ProductVariant oldProductVariant = productVariantsRepository.findById(dto.getId())
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти вариант товара с id = %d для редактирования",dto.getId())));

        if (previewImg == null || previewImg.isEmpty())
            previewImg = oldProductVariant.getPreviewImg();

        //productVariantsRepository.updateProductVariant(dto.getProductId(), dto.getProductId(), previewImg, dto.getTitle(), dto.getPrice());

        ProductVariant pv = new ProductVariant(dto.getId(), dto.getTitle(),
                Services.productsService.getById(dto.getProductId()),
                dto.getPrice(),
                previewImg.isEmpty() ? Constants.EMPTY_IMAGE.toString() : previewImg,
                dto.getShowPv() != null ? dto.getShowPv() : true, null);

        int oldPrice = oldProductVariant.getPrice();
        boolean oldShowState = oldProductVariant.getShowVariant();
        pv = productVariantsRepository.saveAndFlush(pv);

        // После обновления цены варианта, обновить все корзины и необработанные заказы
        if (pv.getPrice() != oldPrice && pv.getShowVariant()) {
            Services.basketsService.updateBasketsOnPvPriceChanged(pv);
            Services.ordersService.updateOrdersOnPvPriceChanged(pv);
        }
        // Не изменили стоимость, но при этом восстановили показ варианта товара
        else if (pv.getPrice() == oldPrice && (!oldShowState && pv.getShowVariant())) {

            Services.basketsService.updateBasketsOnPvDisclosure(pv, null);
            Services.ordersService.updateOrdersOnPvDisclosure(pv, null);

        }
        //Скрытие варианта товара (текущее состояние отличается от прошлого)
        else if (!pv.getShowVariant() && oldShowState){
            Services.basketsService.updateBasketsOnPvHidden(pv, null);
            Services.ordersService.updateOrdersOnPvHidden(pv, null);
        }

        return pv;
    }

    @Override
    public void update(long productVariantId, long productId, String previewImg, String title, int price) {
        productVariantsRepository.updateProductVariant(productVariantId, productId, previewImg, title, price);
    }

    @Override
    public void updatePvDisplay(long productVariantId) {
        ProductVariant pv = productVariantsRepository.findById(productVariantId)
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти вариант товара с id: %d", productVariantId)));

        // Изменить значение на противоположное
        pv.setShowVariant(!pv.getShowVariant());

        pv = productVariantsRepository.saveAndFlush(pv);

        // Разные действия в зависимости от скрытия/открытия варианта
        if (!pv.getShowVariant()) {
            Services.basketsService.updateBasketsOnPvHidden(pv, null);
            Services.ordersService.updateOrdersOnPvHidden(pv, null);
        } else {
            Services.basketsService.updateBasketsOnPvDisclosure(pv, null);
            Services.ordersService.updateOrdersOnPvDisclosure(pv, null);
        }
    }

    // Изменение состояния вариантов товара в соответсвии с изменением состояния самого товаров
    @Override
    public void updateCascadePvDisplay(Product product) {

        // Найти варианты товара, состояние которых отличается от состояния товара (товар скрыт, а варианты - нет)
        List<ProductVariant> pvList = product.getProductVariants()
                .stream()
                .filter(pv -> pv.getShowVariant() == null || pv.getShowVariant() != product.getShowProduct())
                .toList();

        if (pvList.isEmpty())
            return;

        // сохранить изменения вариантов товаров
        pvList.forEach(pv -> pv.setShowVariant(product.getShowProduct()));

        productVariantsRepository.saveAllAndFlush(pvList);

        if (!product.getShowProduct()) {
            Services.basketsService.updateBasketsOnPvHidden(null, pvList);
            Services.ordersService.updateOrdersOnPvHidden(null, pvList);
        } else {
            Services.basketsService.updateBasketsOnPvDisclosure(null, pvList);
            Services.ordersService.updateOrdersOnPvDisclosure(null, pvList);
        }

    }
    //endregion

    //Изменить изображение предосмотра
    @Override
    @Transactional
    public void updatePreview(long productVariantId, String previewImg) {
        productVariantsRepository.updateProductVariantPreview(productVariantId, previewImg);

        // Очистить кэш. Иначе в моменте запись может не изменится, что повлияет на замену или удалиение
        entityManager.flush();
        entityManager.clear();
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

    @Override
    public boolean deleteById(long pvId) {

        ProductVariant foundProductVariant = productVariantsRepository.findById(pvId)
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти производителя с id = %d!",pvId)));

        if (foundProductVariant.getIsDeleted() != null && foundProductVariant.getIsDeleted())
            throw  new ApiException(String.format("Вариант товара с id: %d уже удалён!", pvId));

        foundProductVariant.setIsDeleted(true);

        foundProductVariant = productVariantsRepository.saveAndFlush(foundProductVariant);

        Services.basketsService.updateBasketsOnPvDelete(foundProductVariant, null);
        Services.ordersService.updateOrdersOnPvDelete(foundProductVariant, null);

        return foundProductVariant.getIsDeleted();
    }

    @Override
    public boolean recoverDeletedById(long pvId) {
        ProductVariant foundProductVariant = productVariantsRepository.findById(pvId)
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти производителя с id = %d!",pvId)));

        if (foundProductVariant.getIsDeleted() == null || !foundProductVariant.getIsDeleted())
            throw  new ApiException(String.format("Вариант товара с id: %d не был удалён!", pvId));

        foundProductVariant.setIsDeleted(false);

        return !productVariantsRepository.saveAndFlush(foundProductVariant).getIsDeleted();
    }

    @Override
    public void deleteByProductId(long productId) {
        productVariantsRepository.deleteVariantsByProduct(productId, null);

        List<ProductVariant> changedPv = getByProductId(productId);

        // Восстановить варианты товаров в корзине и заказе
        if (!changedPv.isEmpty()){
            Services.basketsService.updateBasketsOnPvDelete(null, changedPv);
            Services.ordersService.updateOrdersOnPvDelete(null, changedPv);
        }
    }

    @Override
    public void recoverDeletedByProductId(long productId) {
        productVariantsRepository.recoverVariantsByProduct(productId, null);
    }

    @Override
    public void deleteByProductIdList(List<Long> productsIds) {

        if (productsIds == null || productsIds.isEmpty())
            throw new ApiException("Не удалось удалить товары по списку. Список некорректен!");

        productVariantsRepository.deleteVariantsByProductIdList(productsIds);
        //ServicesUtils.deleteOrRecoverVariant(entityManager,null, productsIds, true);
        List<ProductVariant> productVariants = productVariantsRepository.findProductVariantsByProductIdList(productsIds);

        // Убрать все удалённые варианты из корзин и заказов
        if (!productVariants.isEmpty()){
            Services.basketsService.updateBasketsOnPvDelete(null, productVariants);
            Services.ordersService.updateOrdersOnPvDelete(null, productVariants);
        }

    }

    @Override
    public void recoverDeletedByProductIdList(List<Long> productsIds) {

        if (productsIds == null || productsIds.isEmpty())
            throw new ApiException("Не удалось восстановить товары по списку. Список некорректен!");

        productVariantsRepository.recoverVariantsByProductIdList(productsIds);

    }

    @Override
    public void hideByProductsList(List<Product> products) {
        List<ProductVariant> pvList = productVariantsRepository.findProductVariantsByProductIdList(products.stream().map(Product::getId).toList());

        pvList.forEach(pv -> pv.setShowVariant(false));

        productVariantsRepository.saveAllAndFlush(pvList);

        // Произвести перерасчёт сумм в незавершенных заказах и корзинах
        Services.basketsService.updateBasketsOnPvHidden(null, pvList);
        Services.ordersService.updateOrdersOnPvHidden  (null, pvList);
    }

    @Override
    public void hideByProductId(Product product) {

        // Если товар не был скрыт
        if (product.getShowProduct())
            return;

        List<ProductVariant> pvList = getByProductId(product.getId());

        pvList.forEach(pv -> pv.setShowVariant(product.getShowProduct()));

        productVariantsRepository.saveAllAndFlush(pvList);

        // Произвести перерасчёт сумм в незавершенных заказах и корзинах
        Services.basketsService.updateBasketsOnPvHidden(null, pvList);
        Services.ordersService.updateOrdersOnPvHidden  (null, pvList);
    }

    @Override
    public void recoverHiddenByProductsList(List<Product> products) {

        List<ProductVariant> pvList = productVariantsRepository.findProductVariantsByProductIdList(products.stream().map(Product::getId).toList());

        pvList.forEach(pv -> pv.setShowVariant(true));

        productVariantsRepository.saveAllAndFlush(pvList);

        // Произвести перерасчёт сумм в незавершенных заказах и корзинах
        Services.basketsService.updateBasketsOnPvDisclosure(null, pvList);
        Services.ordersService.updateOrdersOnPvDisclosure(null, pvList);
    }

    @Override
    public void recoverHiddenByProductId(Product product) {

        // Если товар скрыт
        if (!product.getShowProduct())
            return;

        List<ProductVariant> pvList = getByProductId(product.getId());

        pvList.forEach(pv -> pv.setShowVariant(product.getShowProduct()));

        productVariantsRepository.saveAllAndFlush(pvList);

        // Произвести перерасчёт сумм в незавершенных заказах и корзинах
        Services.basketsService.updateBasketsOnPvDisclosure(null, pvList);
        Services.ordersService.updateOrdersOnPvDisclosure(null, pvList);
    }

    @Override
    @Transactional
    public void deleteOrRecoverVariant(Long id, List<Long> idList, boolean deletionFlag) {
        if (id == null && idList == null)
            throw new ApiException("В методе удаления/восстановления варианта оба параметра id и idList равны Null!");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaUpdate<ProductVariant> update = cb.createCriteriaUpdate(ProductVariant.class);
        Root<ProductVariant> root = update.from(ProductVariant.class);
        Path<Product> product = root.get("product");

        // Условие для определения обновляемых вариантов
        Predicate predicate;

        if (id != null && idList != null)
            predicate = cb.or(
                    cb.equal(product.get("id"), id),
                    product.get("id").in(idList)
            );
        else if (idList == null)
            predicate = cb.equal(product.get("id"), id);
        else
            predicate = product.get("id").in(idList);

        update.set(root.get("isDeleted"), deletionFlag)
              .where(predicate);

        entityManager.createQuery(update).executeUpdate();

    }

}