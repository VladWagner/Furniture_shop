package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.ProductVariantDto;
import gp.wagner.backend.domain.dto.request.crud.product.ProductImageDto;
import gp.wagner.backend.domain.dto.request.crud.product.ProductImageDtoContainer;
import gp.wagner.backend.domain.dto.response.product_variants.ProductVariantDetailsRespDto;
import gp.wagner.backend.domain.dto.response.product_variants.ProductVariantPreviewRespDto;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductImage;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.ControllerUtils;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping(value = "/api/product_variants")
public class ProductVariantsController {

    //Выборка варианта для конкретного товара
    //Возвращаем список DTO вариантов товаров
    @GetMapping(value = "/by_product/preview/{product_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductVariantPreviewRespDto> getProductVariants(@PathVariable @Min(1) long product_id) {

        if (product_id > Services.productsService.getMaxId())
            throw new ApiException("Id товара задан некорректно!");

        List<ProductVariant> productVariants = Services.productVariantsService.getByProductId(product_id);

        //Создаём из вариантов товаров список объектов DTO для вариантов товаров
        return productVariants.stream().map(ProductVariantPreviewRespDto::new).toList();
    }

    // Выборка варианта для конкретного товара
    // Возвращаем список DTO вариантов товаров
    @GetMapping(value = "/by_product/detailed/{product_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductVariantDetailsRespDto> getProductVariantsDetailed(@PathVariable @Min(1) long product_id) {

        if (product_id > Services.productsService.getMaxId())
            throw new ApiException("Id товара задан некорректно!");

        List<ProductVariant> productVariants = Services.productVariantsService.getByProductId(product_id);

        //Создаём из вариантов товаров список объектов DTO для вариантов товаров + формируем список изображений
        return productVariants.stream()
                .map(ProductVariantDetailsRespDto::new).toList();
    }

    //Получить конкретный вариант

    @GetMapping(value = "/variant_by_id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductVariantDetailsRespDto getProductVariant(@PathVariable @Min(1) long id) {

        if (id > Services.productVariantsService.getMaxId())
            throw new ApiException("Id варианта товара задан некорректно!");

        ProductVariant productVariant = Services.productVariantsService.getById(id);

        //Формируем DTO из самого конкретного варианта продукта и изображений под его id
        //При этом формируем ещё список DTO для изображений варианта товара,
        // поскольку там будут заданы ссылка на изображение + порядковый номер
        return new ProductVariantDetailsRespDto(productVariant);
    }

    //Добавить вариант товара
    @PostMapping()
    public String createProductVariant(@Valid @RequestPart(value = "product_variant") ProductVariantDto productVariantDto,
                                       @RequestPart(value = "files") List<MultipartFile> files) {

        long createdProductVariantId = 0;
        try {

            Product product = Services.productsService.getById(productVariantDto.getProductId());

            if (files.size() == 0 || product == null)
                throw new ApiException(String.format("Загрузить медиафайлы не удалось или товар с id: %d не существует!", productVariantDto.getProductId()));

            List<String> filesUris = new ArrayList<>();

            //Загрузка файлов
            for (MultipartFile file : files) {

                //Метод заменяет \ на / и убирает .. в заданном пути
                String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

                //Добавить путь файла в список uri, который будет писаться в БД
                filesUris.add(
                        Utils.cleanUrl(
                                Services.fileManageService.saveFile(fileName, file, product.getCategory().getId(), productVariantDto.getProductId()).toString())
                );
            }

            //Добавление превью для варианта товара - первое заданное изображение

            Resource thumbnailUri = Services.fileManageService.saveThumbnail(filesUris.get(0), product.getCategory().getId(), productVariantDto.getProductId());

            //Добавление варианта товара
            createdProductVariantId = Services.productVariantsService.create(productVariantDto,
                    thumbnailUri != null ? Utils.cleanUrl(thumbnailUri.toString()) : "");

            //Если создать вариант товара не удалось
            if (createdProductVariantId <= 0)
                throw new Exception(String.format("Product variant for product with id %d is not created", productVariantDto.getProductId()));

            //Добавление изображений в таблицу по определённому (в нашем случае базовому варианту товара)
            for (int i = 0; i < filesUris.size(); i++)
                Services.productImagesService.save(createdProductVariantId, filesUris.get(i), i + 1);

        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }

        return String.format("Product variant for product with id: %d добавлен! Id of product variant: %d",
                productVariantDto.getProductId(), createdProductVariantId);

    }

    //Изменить вариант товара
    @PutMapping()
    public String updateProductVariant(@Valid @RequestPart(value = "product_variant") ProductVariantDto pvDto,
                                       @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                       @RequestPart(value = "images_order") ProductImageDtoContainer container) throws Exception {


        // Найти товар, для которого производится редактирование варианта
        Product product = Services.productsService.getById(pvDto.getProductId());

        if (product == null)
            throw new ApiException(String.format("Для редактирования варианта не найден сам товар с id: %d", pvDto.getId()));

        //Если в процессе редактирования товара просто поменяли изображения местами
        if (files == null)
            files = new ArrayList<>();

        //Редактирование товара
        ProductVariant changedVariant = Services.productVariantsService.update(pvDto, null);

        //Общие для всех записей изображений id категории и товара - нужно при формировании каталогов
        Utils.CategoryAndProductIds categoryAndProductIds = new Utils.CategoryAndProductIds(product.getCategory().getId(), pvDto.getProductId());

        List<ProductImage> productImages = Services.productImagesService.getByProductVariantId(pvDto.getId());

        //Id изображений, которые были заменены, чтобы избежать неправильного удаления по id
        List<Long> changedImages = new ArrayList<>();

        //Найти минимальное значение порядка вывода изображения - первое изображение
        int minOrderValue = ControllerUtils.getMinImgOrderValue(productImages, container.productImageDtoList());

        //Пройти по всем загруженным файлам собственно для загрузки на сервер
        for (ProductImageDto imageDto : container.productImageDtoList()) {

            //Найти нужный файл по имени, заданном в dto изображений
            MultipartFile file = files.stream()
                    .filter(f -> Objects.equals(f.getOriginalFilename(), imageDto.getFileName()))
                    .findFirst().orElse(null);

            //Если файл загружен не был и при этом его путь задан в dto, то возможно это изменение порядка
            if (file == null) {

                // Получить объект изображения по od
                ProductImage productImage = imageDto.getId() != null ? Services.productImagesService.getById(imageDto.getId()) : null;

                // Если найти изображение по id не удалось и при этом задано имя файла
                if (productImage == null && !imageDto.getFileName().isBlank())
                    productImage = Services.productImagesService.getByLink(imageDto.getFileName());

                //Если такого изображения нет, или оно не для этого варианта товара
                if (productImage == null || productImage.getProductVariant().getId() != pvDto.getId())
                    continue;

                // Выполнить логику замены изображений местами
                ControllerUtils.changeImagesOrder(imageDto, productImage, changedVariant, minOrderValue);

                continue;
            }

            //Получить имя загружаемого файла, заменив \ на / и убрав ..
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            //Если файл первый по порядку, тогда нужно заменить само изображение и uri
            if (imageDto.getImgOrder() <= minOrderValue) {
                ControllerUtils.loadNewImgWithThumb(productImages, fileName, file, changedVariant, categoryAndProductIds, imageDto, changedImages);
                minOrderValue = imageDto.getImgOrder();
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
                    file, product.getCategory().getId(),
                    pvDto.getProductId()).toString()
            );

            //Если запись с заданным порядковым номером имеется - заменить uri и удалить старое изображение
            if (productImage != null) {

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
                changedImages.add(Services.productImagesService.save(pvDto.getId(), fileUri, imageDto.getImgOrder()));
        }//for

        //Получить список объектов сущности таблицы изображений, которые нужно удалять
        List<ProductImage> deletingProductImages = Services.productImagesService.getByIdList(container.deletedImagesId());

        //Удалить изображения вариантов товаров заданные в список dto
        if (deletingProductImages != null)
            ControllerUtils.deleteProductVariantImages(deletingProductImages, changedImages, changedVariant);


        return String.format("Вариант товара с id: %d успешно изменён!", pvDto.getId());

    }

    // Удалить вариант товара
    @DeleteMapping(value = "/delete_by_id/{id}")
    public ResponseEntity<String> deletePvById(@PathVariable @Min(1) long id) {

        boolean isDeleted = Services.productVariantsService.deleteById(id);

        return new ResponseEntity<>(String.format("Вариант товара с id %d %s удалить!", id, isDeleted ? "удалось" : "не удалось"),
                HttpStatusCode.valueOf(isDeleted ? 200 : 500));

    }

    // Восстановить вариант товара
    @PutMapping(value = "/recover_by_id/{id}")
    public ResponseEntity<String> recoverPvById(@PathVariable @Min(1) long id) {

        boolean isRecovered = Services.productVariantsService.recoverDeletedById(id);

        return new ResponseEntity<>(String.format("Вариант товара с id %d %s восстановить!", id, isRecovered ? "удалось" : "не удалось"),
                HttpStatusCode.valueOf(isRecovered ? 200 : 500));

    }

}
