package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.dto.request.crud.AttributeValueDto;
import gp.wagner.backend.domain.dto.request.crud.product.ProductDto;
import gp.wagner.backend.domain.dto.request.crud.product.ProductImageDto;
import gp.wagner.backend.domain.dto.response.categories.CategoryBreadcrumbsDto;
import gp.wagner.backend.domain.dto.response.categories.CategoryDtoWithChildren;
import gp.wagner.backend.domain.dto.response.category_views.CategoriesViewsWithChildrenDto;
import gp.wagner.backend.domain.dto.response.products.ProductPreviewRespDto;
import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.products.ProductImage;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.domain.entities.visits.CategoryViews;
import gp.wagner.backend.middleware.Services;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.*;

public class ControllerUtils {

    //Сформировать список объектов ProductPreviewRespDto для отправки на клиента
    //TODO: такой тип выборки ужасно прожорлив и нужно срочно разбираться с ManyToOne <--> OneToMany,
    //поскольку здесь каждый раз происходит запрос к БД на каждой итерации
    public static List<ProductPreviewRespDto> getProductsPreviewsList(List<Product> products){

        if (products == null)
            return null;
        return products.stream().map(ProductPreviewRespDto::new).toList();
    }//getProductsPreviewsList

    //Добавление характеристик товара
    public static void addProductFeatures(long productId, List<AttributeValueDto> attributeValueDtoList){
        if (attributeValueDtoList != null && !attributeValueDtoList.isEmpty())
            //Пройти по списку характеристик и задать DTO в метод сервиса
            for (AttributeValueDto dto: attributeValueDtoList)
                Services.attributeValuesService.save(productId, dto);

    }

    //Изменение характеристик товара
    public static void updateProductFeatures(ProductDto productDto){
        //В DTO товара находится список атрибутов
        if (productDto.getAttributes() != null && !productDto.getAttributes().isEmpty())

            //Пройтись по списку атрибутов и определить, происходит ли добавление или изменение существующего атрибута
            for (AttributeValueDto dto: productDto.getAttributes()) {
                if (dto.getId() != null)
                    Services.attributeValuesService.update(dto);
                else
                    Services.attributeValuesService.save(productDto.getId(),dto);
            }

        //Если нужно удалить какие-либо характеристики после редактирования товара
        if (productDto.getDeletedAttributesValues().length > 0)
            Services.attributeValuesService.deleteByIdList(Arrays.stream(productDto.getDeletedAttributesValues()).toList());

    }

    //Загрузка нового изображения с созданием preview
    //Принимает: список объектов сущности таблицы изображений товаров -> название загружаемого файла -> собственно файл -> объект варианта товара ->
    // dto товара -> dto изображения товара -> порядковые номера измененных изображений
    public static void loadNewImgWithThumb(List<ProductImage> productImages,
                                           String loadFileName,
                                           MultipartFile multipartFile,
                                           ProductVariant productVariant,
                                           Utils.CategoryAndProductIds ids,
                                           ProductImageDto imageDto,
                                           List<Long> changedImages) throws Exception {

        if (multipartFile == null)
            return;

        //Загрузить новый файл thumb и убрать все приставки "URL=" из адреса -
        String fileUri = Utils.cleanUrl(Services.fileManageService.saveProductImgFile(loadFileName, multipartFile, ids.categoryId(), ids.productId()).toString());

        if (productImages.size() > 0) {

            //Выбрать 1-е изображение - по которому будет создаваться preview
            ProductImage productImage = productImages.get(0);

            //Удалить изображение из папки - поскольку мы меняем основную картинку и thumb
            Services.fileManageService.deleteFile(new URI(productImage.getImgLink()));

            //Удалить превью
            Services.fileManageService.deleteFile(new URI(productVariant.getPreviewImg()));

            //Обновить запись об изображении
            Services.productImagesService.update(productImage.getId(), fileUri, imageDto.getImgOrder());

            changedImages.add(productImage.getId());
        }
        else changedImages.add(Services.productImagesService.save(productVariant.getId(),fileUri, imageDto.getImgOrder()));

        String thumbNailUri = Utils.cleanUrl(Services.fileManageService.saveThumbnail(fileUri, ids.categoryId(), ids.productId()).toString());
        //Загрузить preview
        Services.productVariantsService.updatePreview(productVariant.getId(), thumbNailUri);
        productVariant.setPreviewImg(thumbNailUri);
    }

    //Удалить изображения заданные в списке и его thumbnail, если изображение первое
    public static void deleteAtReplaceThumb(URI fileUri, ProductVariant productVariant, ProductImage deletingImg, Utils.CategoryAndProductIds ids) throws Exception {
        //Удалить изображение
        Services.fileManageService.deleteFile(fileUri);

        //Удалить thumbnail
        Services.fileManageService.deleteFile(new URI(productVariant.getPreviewImg()));

        //Удалить запись об изображении
        Services.productImagesService.deleteById(deletingImg.getId());

        //Найти следующее изображение и изменить его порядковый номер вывода

        List<ProductImage> nextImagesList = Services.productImagesService.getByProductVariantId(productVariant.getId());

        //Найти порядковое изображение с минимальным порядковым номером
        ProductImage nextImage = nextImagesList != null && nextImagesList.size() > 0 ?
                nextImagesList.stream()
                        .min(Comparator.comparing(ProductImage::getImgOrder))
                        .get() : null;

        //Если изображений больше не осталось - не на что заменять
        if (nextImage == null) {
            Services.productVariantsService.updatePreview(productVariant.getId(), "empty_img");
            return;
        }

        //Задать порядковый номер первого изображения после удалённого
        nextImage.setImgOrder(1);
        Services.productImagesService.update(nextImage);

        //Создать thumbnail и добавить его варианту
        String thumbnailUri = Utils.cleanUrl(Services.fileManageService.saveThumbnail(nextImage.getImgLink(), ids.categoryId(), ids.productId()).toString());
        Services.productVariantsService.updatePreview(productVariant.getId(), thumbnailUri);
    }

    //Получить минимальное значение номера вывода изображения по порядку.
    //Получаем мин.значение либо из списка фото под конкретный вариант товара, либо из списка загруженных изображений
    //-1 означает, что получить значение не удалось никаким способом
    public static int getMinImgOrderValue(List<ProductImage> productImages, List<ProductImageDto> imageDtoList){
        return productImages.size() > 0 ? productImages.stream()
                .min(Comparator.comparingInt(ProductImage::getImgOrder))
                .get().getImgOrder() :
                imageDtoList.size() > 0 ? imageDtoList.stream()
                        .min(Comparator.comparingInt(ProductImageDto::getImgOrder))
                        .get().getImgOrder() : -1;
    }

    // Найти дочерние категории
    public static List<CategoryDtoWithChildren> findChildCategories(long parentId){

        List<CategoryDtoWithChildren> categories = Services.categoriesService.getChildCategories(parentId)
                .stream()
                .map(CategoryDtoWithChildren::factory)
                //.map(c -> new CategoryDtoWithChildren(c, null))
                .toList();

        if (categories.isEmpty())
            return null;

        // Для дочерних категорий так же найти их вложенные категории
        for (CategoryDtoWithChildren categoryDto : categories) {
            categoryDto.setChildCategories(findChildCategories(categoryDto.getId()));
        }

        return categories;
    }

    // Найти дочерние категории, которые есть в заданной ассоциативной коллекции
    public static List<CategoryDtoWithChildren> findChildCategories(long parentId, Set<Long> categoriesIdsSet){

        List<CategoryDtoWithChildren> categories = Services.categoriesService.getChildCategories(parentId)
                .stream()
                .filter(c -> categoriesIdsSet.contains(c.getId()))
                //.map(CategoryDtoWithChildren::factory)
                .map(c -> new CategoryDtoWithChildren(c, null))
                .toList();

        if (categories.isEmpty())
            return null;

        // Для дочерних категорий так же найти их вложенные категории
        for (CategoryDtoWithChildren categoryDto : categories) {
            categoryDto.setChildCategories(findChildCategories(categoryDto.getId(), categoriesIdsSet));
        }

        return categories;
    }

    //Рекурсивный обход дерева категорий
    public static CategoriesViewsWithChildrenDto findSubCategoryViews(long categoryId){

        //Получить значения просмотров для заданной категории, суммируя просмотры дочерних категорий на всех уровнях рекурсии
        CategoryViews categoryViews = Services.categoryViewsService.getSimpleCVByCategoryId(categoryId);

        //Создать DTO под просмотр данной категории
        CategoriesViewsWithChildrenDto categoriesViewsDto = new CategoriesViewsWithChildrenDto(categoryViews);

        //Найти дочерние элементы на одном уровне рекурсии
        List<Long> childCategories = Services.categoriesService.getChildCategoriesIds(categoryId);

        //Для каждого дочернего элемента, найти его дочерние элементы, пока не дойдём до последней

        for (Long id : childCategories)
            categoriesViewsDto.childCategories.add(findSubCategoryViews(id));

        return categoriesViewsDto;
    }

    // Удалить изображения заданные в DTO при редактировании варианта товара

    public static void deleteProductVariantImages(List<ProductImage> deletingProductImages, List<Long> changedImages,
                                                  ProductVariant variant) throws Exception {

        Utils.CategoryAndProductIds categoryAndProductIds = new Utils.CategoryAndProductIds(variant.getProduct().getCategory().getId(), variant.getProduct().getId());

        //ProductVariant variant;
        for (ProductImage prodImage : deletingProductImages) {

            //Проверить, не изменялись ли удаляемые изображения
            // (нажали кнопку удаления и после добавили новое изображение, а id остался в списке)
            if (changedImages.contains(prodImage.getId()))
                continue;

            URI fileUri = new URI(prodImage.getImgLink());

            // Для проверки, является ли удаляемое изображение preview варианта товара
            // Каждый раз получаем один и тот же экземпляр варианта товара из-за того, что при удалении изображения, которое является preview,
            // тогда оно меняется и для варианта товара на следующее, которое так же может быть удаляемым. Следовательно и для него нужно удалить preview
            variant = Services.productVariantsService.getById(variant.getId());

            //Получить путь к файлу предосмотра для варианта товара
            String variantPreview = variant.getPreviewImg();

            //Убрать из имени изображения путь и приставку thumb, чтобы можно былой найти основное изображение по названию
            variantPreview = variantPreview.substring(Utils.findLastIndex(variantPreview, "/\\")+1)
                    .replace(Constants.THUMB_SUFFICE, "");

            //Получить имя удаляемого изображения - убрать путь в названии
            String prodImageName = prodImage.getImgLink().substring(Utils.findLastIndex(prodImage.getImgLink(), "/\\")+1);

            //Если удаляемое изображение, является первым изображением - изображение предосмотра
            //всегда содержит в себе название основного
            if (variantPreview.contains(prodImageName)){

                // Удалить preview, внутри preview будет заменено на следующее по порядку изображение, пока они не закончатся
                deleteAtReplaceThumb(fileUri, variant, prodImage, categoryAndProductIds);
                continue;
            }

            Services.fileManageService.deleteFile(fileUri);

            Services.productImagesService.deleteById(prodImage.getId());

        }//for
    }

    // Поменять порядок вывода изображений
    public static void changeImagesOrder(ProductImageDto imageDto, ProductImage existingImage, ProductVariant pv, int minOrderValue) throws Exception {

        //Найти изображение с заданным в DTO порядковым номером
        ProductImage imageByOrder = Services.productImagesService.getByVariantIdAndByOrder(pv.getId(), imageDto.getImgOrder());

        //Если изображение с таким порядковым номером имеется и при этом не есть тем же изображением
        if (imageByOrder != null && !imageByOrder.getId().equals(existingImage.getId())){
            //"Поменять изображения местами"
            imageByOrder.setImgOrder(existingImage.getImgOrder());
            Services.productImagesService.update(imageByOrder);
        }

        existingImage.setImgOrder(imageDto.getImgOrder());

        Services.productImagesService.update(existingImage);

        //Если при замене порядкового номера изображение стало первым, тогда создать thumbnail
        if (existingImage.getImgOrder() <= minOrderValue) {

            //Удалить предыдущее thumbnail
            String oldPreview = Services.productVariantsService.getById(pv.getId()).getPreviewImg();

            Services.fileManageService.deleteFile(new URI(oldPreview));

            String thumbnailUri = Utils.cleanUrl(Services.fileManageService.saveThumbnail(existingImage.getImgLink(),
                    pv.getProduct().getCategory().getId(), pv.getProduct().getId()
                    ).toString());
            Services.productVariantsService.updatePreview(pv.getId(), thumbnailUri);
        }
    }

    // Получить breadcrumbs по категории
    public static CategoryBreadcrumbsDto createBreadCrumbByCategory(Category category) {

        if (category == null)
            return null;

        Set<Long> ids = new HashSet<>();
        ids.add(category.getId());

        Category parentCategory = category;

        // Подняться к родительской категории
        while (parentCategory.getParentCategory() != null){
            parentCategory = parentCategory.getParentCategory();
            ids.add(parentCategory.getId());
        }

        return breadcrumbsFromParentCategory(parentCategory, ids);
    }

    private static CategoryBreadcrumbsDto breadcrumbsFromParentCategory(Category parent, Set<Long> childrenIds){

        CategoryBreadcrumbsDto categoryBreadcrumbsDto = new CategoryBreadcrumbsDto(parent);

        // Получить дочерние категории текущей на одном уровне рекурсии
        Category childCategory = Services.categoriesService.getChildCategories(parent.getId())
                .stream()
                .filter(c -> childrenIds.contains(c.getId()))
                .findFirst()
                .orElse(null);

        // Если дочерней категории нет, тогда мы дошли до нужной
        if (childCategory == null)
            return categoryBreadcrumbsDto;

        // Спустится ещё на 1 уровень рекурсии
        categoryBreadcrumbsDto.setChildBreadCrumb(breadcrumbsFromParentCategory(childCategory, childrenIds));

        return categoryBreadcrumbsDto;
    }

    // Засчитать просмотр категории
    public static void countCategoryView(HttpServletRequest request, Long categoryId){
        String fingerprint = Utils.getFingerprint(request);
        if (fingerprint != null && categoryId != null) {
            if (categoryId > 0)
                Services.categoryViewsService.createOrUpdate(fingerprint, request.getRemoteAddr(), categoryId);
            else if (categoryId < 0)
                Services.categoryViewsService.createOrUpdateRepeatingCategory(fingerprint, request.getRemoteAddr(), categoryId);
        }
    }
}