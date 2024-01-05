package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.product.ProductDto;
import gp.wagner.backend.domain.dto.request.crud.product.ProductImageDto;
import gp.wagner.backend.domain.dto.request.crud.product.ProductImageDtoContainer;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.dto.response.AttributeValueRespDto;
import gp.wagner.backend.domain.dto.response.product.ProductDetailsRespDto;
import gp.wagner.backend.domain.dto.response.product.ProductPreviewRespDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.ProductImage;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.ControllerUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.domain.entites.products.Product;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.*;


@RestController
@RequestMapping(value = "/api/products")
@Slf4j
public class ProductsController {

    //Выборка всех товаров без пагинации
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductPreviewRespDto> getAllProducts(){
        List<Product> products = Services.productsService.getAll();

        //Именно здесь и находится проблема с многократным обращением к справочны таблицам при выборке каждого продукта
        List<ProductPreviewRespDto> productsDto = ControllerUtils.getProductsPreviewsList(products);

        return productsDto;
    }

    //Выборка всех товаров с пагинацией
    @GetMapping(value = "/",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map.Entry<Long, List<ProductPreviewRespDto>>  getAllProductsPaged(
                                             @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
                                             @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
                                             @RequestParam(value = "sort", defaultValue = "price_desc") String sortType){

        Page<Product> productsPage = Services.productsService.getAll(pageNum - 1, limit);

        List<ProductPreviewRespDto> productsDto = ControllerUtils.getProductsPreviewsList(productsPage.getContent());

        return new AbstractMap.SimpleEntry<>(productsPage.getTotalElements(), productsDto);
    }

    //Выборка всех товаров по категории с пагинацией
    @GetMapping(value = "/by_category",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map.Entry<Long, List<ProductPreviewRespDto>>  getProductsByCategoryPaged(
                                             @RequestParam(value = "category_id", defaultValue = "1") long categoryId,
                                             @RequestParam(value = "fingerprint") String fingerprint,
                                             @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
                                             @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
                                             @RequestParam(value = "sort", defaultValue = "price_desc") String sortType){

        Category requiredCategory = Services.categoriesService.getById(categoryId);

        //Совершить подсчёт кол-а просмотров, только если это не общая категория
        if (requiredCategory.getParentCategory() == null)
            throw new ApiException("I can't look for products by general category!");

        Services.categoryViewsService.createOrUpdate(fingerprint, categoryId);

        Page<Product> productsPage = Services.productsService.getByCategory(categoryId,pageNum - 1, limit);

        List<ProductPreviewRespDto> productsDto = ControllerUtils.getProductsPreviewsList(productsPage.getContent());

        return new AbstractMap.SimpleEntry<>(productsPage.getTotalElements(), productsDto);
    }

    //Выборка с фильтрами + пагинация фильтрации
    //В DTO фильтра передаётся: значения и типы характеристик товара, заданные по модели EAV
    //Возвращается: количество данных + список товаров на странице в виде пары ключ + значение
    /**
     * Выборка с фильтрами + пагинация фильтрации
     * В DTO фильтра передаётся: значения и типы характеристик товара, заданные по модели EAV
     * Возвращается: количество данных + список товаров на странице в виде пары ключ + значение
     * */
    @GetMapping(value = "/filter",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map.Entry<Long, List<ProductPreviewRespDto>> getProductsPaged(
            @Valid @RequestPart(value = "filter") ProductFilterDtoContainer container,
            @Valid @RequestParam(value = "offset") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit") @Max(80) int limit,
            @RequestParam(value = "category_id")  Long categoryId,
            @RequestParam(value = "price_range", defaultValue = "") String priceRange){

        //Container - список DTO с условиями фильтрации и логическими операциями (or/and)
        SimpleTuple<List<Product>, Integer> productsTuple = Services.productsService.getAll(container, categoryId < 1 ? null : categoryId,
                priceRange.isEmpty() ? null : priceRange,
                pageNum, limit);

        List<ProductPreviewRespDto> productsDto = ControllerUtils.getProductsPreviewsList(productsTuple.getValue1());

        //Фильтрация по диапазону стоимостей и категория производятся по данным, полученным в отдельных параметрах, не через DTO фильтра

        return new AbstractMap.SimpleEntry<>(productsTuple.getValue2().longValue(), productsDto);
    }

    //Выборка атрибутов товаров по id
    //В параметрах передаём id товара для выборки значений его характеристик
    //Возвращает пару ключ-значение с названием продукта + его характеристиками
    @GetMapping(value = "/{id}/characteristics", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map.Entry<String, List<AttributeValueRespDto>> getProductsCharacteristics(@PathVariable long id){

        Product product = Services.productsService.getById(id);

        return new AbstractMap.SimpleEntry<>(product.getName(), ControllerUtils.getAttributesValues(product.getId()));
    }

    //Выборка товара по id
    //Produces - тип возвращаемого значения, в данном случае возвращаем продукт в виде JSON
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductDetailsRespDto getProductById(@PathVariable int id,
                                                @Nullable @RequestParam(value = "fingerprint", defaultValue = "") String fingerPrint){

        Product product = Services.productsService.getById((long) id);

        //Увеличить значение в счётчике просмотров каждого продукта
        if (product != null && fingerPrint != null)
            Services.productViewService.createOrUpdate(fingerPrint, product.getId());
        else if (product == null)
            throw new ApiException(String.format("Не удалось выбрать товар с id: %d", id));
        return ProductDetailsRespDto.factory(product);
    }

    //Добаление товара и его базового варианта (для которого будут задаваться начальные характеристики и изображения)
    @PostMapping()
    public String createProduct(@Valid @RequestPart(value = "product") ProductDto productDto,
                                @RequestPart(value = "files") List<MultipartFile> files){
        long createdProductId;

        try {

            if (files.size() == 0)
                throw new ApiException("Загрузить медиафайлы не удалось!");

            List<String> filesUris = new ArrayList<>();

            //Загрузка файлов
            for (MultipartFile file:files) {

                //Метод заменяет \ на / и убирает .. в заданном пути
                String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

                //Добавить путь файла в список uri, который будет писаться в БД
                filesUris.add(
                        Utils.cleanUrl(
                                Services.fileManageService.saveFile(fileName, file, productDto.getCategoryId(), productDto.getId()).toString())
                );
            }

            //Добавление товара
            createdProductId = Services.productsService.create(productDto);

            //Добавление характеристик товара
            ControllerUtils.addProductFeatures(createdProductId, productDto.getAttributes());

            //Добавление базового варианта товара и превью для него

            Resource thumbnailUri = Services.fileManageService.saveThumbnail(filesUris.get(0), productDto.getCategoryId(), productDto.getId());

            long createdProductVariantId = Services.productVariantsService.save(createdProductId,
                    thumbnailUri != null ? Utils.cleanUrl(thumbnailUri.toString()) : "",
                    productDto.getVariantTitle(),
                    productDto.getPrice());

            //Добавление изображений в таблицу по определённому (в нашем случае базовому варианту товара)
            for (int i = 0; i < filesUris.size(); i++)
                Services.productImagesService.save(createdProductVariantId, filesUris.get(i),i+1);

        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }

        return String.format("Товар с id: %d добавлен!", createdProductId);

    }

    //Изменение товара и его базового варианта
    //В параметрах передаётся порядок изображений, которые либо заменяются, либо меняется порядок их вывода
    //Так же здесь передаётся dto-шка товара с измененным значениям характеристик, цен и т.д.
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateProduct(@Valid @RequestPart(value = "product") ProductDto productDto,
                                @RequestPart(value = "files") @Nullable List<MultipartFile> files,
                                @RequestPart(value = "images_order") ProductImageDtoContainer container){

        try {

            Long productVariantId = productDto.getVariantId();

            if (productVariantId == null)
                productVariantId = Services.productVariantsService.getByProductId(productDto.getId()).get(0).getId();

            ProductVariant basicVariant = Services.productVariantsService.getById(productVariantId);

            //Изменить базовый вариант продукта - характеристики, цена и т.д.
            //Базовый вариант потому что, любой товар всегда имеет хотя бы один вариант
            Services.productVariantsService.update(basicVariant.getId(),
                    productDto.getId(),
                    basicVariant.getPreviewImg(),
                    productDto.getVariantTitle(),
                    productDto.getPrice());

            List<ProductImage> productImages = Services.productImagesService.getByProductVariantId(basicVariant.getId());

            //Id изображений, которые были заменены, чтобы избежать неправильного удаления по id
            List<Long> changedImages = new ArrayList<>();

            //Общие для всех записей изображений id категории и изображения
            Utils.CategoryAndProductIds categoryAndProductIds = new Utils.CategoryAndProductIds(productDto.getCategoryId(), productDto.getId());

            //Найти минимальное значение порядка вывода изображения - первое изображение.
            //Найти в коллекции под конкретный вариант, либо получить минимальное значение из коллекции, переданной в запросе
            int minOrderValue = ControllerUtils.getMinImgOrderValue(productImages, container.productImageDtoList());

            //Пройти по всем загруженным файлам
            for (ProductImageDto imageDto : container.productImageDtoList()) {

                //Найти нужный файл по имени, заданном в dto изображений
                MultipartFile file = files == null ? null : files.stream()
                        .filter(f -> Objects.equals(f.getOriginalFilename(), imageDto.getFileName()))
                        .findFirst().orElse(null);

                if (file == null)
                    continue;

                //Получить имя загружаемого файла, заменив \ на / и убрав ..
                String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

                //Если файл первый по порядку, тогда нужно заменить само изображение и uri
                if (imageDto.getImgOrder() <= minOrderValue){
                    ControllerUtils.loadNewImgWithThumb(productImages, fileName, file, basicVariant,
                            categoryAndProductIds,
                            imageDto, changedImages);
                    continue;
                }

                //Найти изображение с заданным порядковым номером (если такового нет, то добавляем)
                //То есть порядковый номер в данном случае выступает в роли идентификатора изображения
                Optional<ProductImage> productImageOptional = productImages.size() > 0 ? productImages.stream()
                        .filter(pi -> pi.getImgOrder() == imageDto.getImgOrder())
                        .findFirst() : Optional.empty();
                ProductImage productImage = productImageOptional.orElse(null);

                //Загрузить изображение в папку, получив URL загруженного изображения
                String fileUri = Utils.cleanUrl(Services.fileManageService.saveFile(fileName,
                        file, productDto.getCategoryId(),
                        productDto.getId()).toString()
                );

                //Если запись с заданным порядковым номером имеется - заменить uri и удалить старое изображение
                if (productImage != null){

                    //Сначала удалить изображение
                    if (!productImage.getImgLink().isBlank())
                        Services.fileManageService.deleteFile(new URI(productImage.getImgLink()));

                    //Добавить uri загруженного изображения в таблицу ProductImages
                    Services.productImagesService.update(productImage.getId(), fileUri, imageDto.getImgOrder());

                    //Задать id изображения, которое было заменено
                    changedImages.add(productImage.getId());

                }
                //Если изображения с заданным порядковым номером для заданного варианта товара нет, тогда добавить ссылку на изображение в БД
                else
                    changedImages.add(Services.productImagesService.save(basicVariant.getId(), fileUri, imageDto.getImgOrder()));
            }//for

            //Получить список объектов сущности таблицы изображений, которые нужно удалять
            List<ProductImage> deletingProductImages = Services.productImagesService.getByIdList(container.deletedImagesId());

            //Удалить изображения вариантов товаров заданные в список dto
            if (deletingProductImages != null) {
                ProductVariant variant;
                for (ProductImage prodImage : deletingProductImages) {

                    //Проверить, не изменялись удаляемые изображения
                    // (нажали кнопку удаления и после добавили новое изображение, а id остался в списке)
                    if (changedImages.contains(prodImage.getId()))
                        continue;

                    URI fileUri = new URI(prodImage.getImgLink());

                    //Для проверки, является ли удаляемое изображение preview БАЗОВОГО варианта товара, у каждого товара имеется базовая версия
                    variant = Services.productVariantsService.getById(productVariantId);

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
                        //Удалить изображение и его preview, внутри preview будет заменено на следующее по порядку изображение, пока они не закончатся
                        ControllerUtils.deleteAtReplaceThumb(fileUri, variant, prodImage,categoryAndProductIds);
                        continue;
                    }

                    Services.fileManageService.deleteFile(fileUri);

                    Services.productImagesService.deleteById(prodImage.getId());

                }//for
            }

            //Редактирование товара (по значениям, переданным в DTO)
            Services.productsService.update(productDto);

            //Добавление/изменение характеристик товара
            ControllerUtils.updateProductFeatures(productDto);

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new ApiException(e.toString());
        }

        return String.format("Товар с id: %d успешно изменён!", productDto.getId());

    }

}
