package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.ProductAttributeRequestDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.product_attributes.ProductAttributeRespDto;
import gp.wagner.backend.domain.entites.eav.ProductAttribute;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/product_attributes")
public class ProductAttributesController {

    // Получить список пользователей (только для админа/модератор)
    @GetMapping(value = "/all")
    public PageDto<ProductAttributeRespDto> getAllAttributes(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                             @Valid @RequestParam(value = "limit") @Max(80) int limit) {

        Page<ProductAttribute> productAttributesPage = Services.productAttributesService.getAll(pageNum, limit);

        // Возвращаться будут все атрибуты и скрытие и нет. Скрытие производить на фронте, чтобы можно было сразу посмотреть скрытые
        return new PageDto<>(
                productAttributesPage,
                () -> productAttributesPage.getContent()
                        .stream()
                        .map(ProductAttributeRespDto::new)
                        .sorted(Comparator.comparing(ProductAttributeRespDto::getPriority))
                        .toList()
        );
    }

    // Получить список атрибутов товаров по id категории
    @GetMapping(value = "/get_by_category")
    public PageDto<ProductAttributeRespDto> getByCategory(
            @Valid @RequestParam(value = "category_id") @Min(0) long categoryId,
            @Valid @RequestParam(value = "offset") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit") @Max(80) int limit) {

        Page<ProductAttribute> productAttributesPage = Services.productAttributesService.getByCategoryId(categoryId, pageNum, limit, null);

        return new PageDto<>(
                productAttributesPage,
                () -> productAttributesPage.getContent().stream()
                        .map(ProductAttributeRespDto::new)
                        .sorted(Comparator.comparing(ProductAttributeRespDto::getPriority))
                        .toList()
        );
    }

    // Получить атрибутов товаров по id
    @GetMapping(value = "/get_by_id/{attribute_id}")
    public ResponseEntity<ProductAttributeRespDto> getById(@Valid @PathVariable(value = "attribute_id") @Min(0) long id) {

        ProductAttribute foundAttribute = Services.productAttributesService.getById(id);

        return ResponseEntity.ok(new ProductAttributeRespDto(foundAttribute));
    }

    // Добавить атрибут
    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAttributeRespDto> createAttribute(
            @Valid @RequestPart(value = "attribute") ProductAttributeRequestDto productAttributeDto
    ) {

        ProductAttribute createdAttribute = Services.productAttributesService.create(productAttributeDto);

        return ResponseEntity.ok(new ProductAttributeRespDto(createdAttribute));

    }

    // Изменить атрибут
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAttributeRespDto> updateAttribute(
            @Valid @RequestPart(value = "attribute") ProductAttributeRequestDto productAttributeDto
    ) {

        ProductAttribute updatedAttribute = Services.productAttributesService.update(productAttributeDto);

        return ResponseEntity.ok(new ProductAttributeRespDto(updatedAttribute));
    }

    // Изменить приоритеты атрибутов
    @PutMapping(value = "/update_priorities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateAttributesPriorities(@RequestBody Map<Long, Float> attributesAndPriorities) {

        Services.productAttributesService.updatePriority(attributesAndPriorities);

        return ResponseEntity.ok("Приоритеты атрибутов успешно изменены!");
    }

    // Скрыть атрибут
    @PutMapping(value = "/hide_attribute/{attribute_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> hideAttribute(@Valid @PathVariable(value = "attribute_id") @Min(0) long id) {

        Services.productAttributesService.hideProductAttribute(id);

        return ResponseEntity.ok(String.format("Атрибут с id %d успешно скрыт!", id));
    }

    // Скрыть список атрибутов
    @PutMapping(value = "/hide_attributes_list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> hideAttributesList(@RequestBody List<Long> attributesIdsList) {

        Services.productAttributesService.hideProductAttributesList(attributesIdsList);

        return ResponseEntity.ok("Атрибуты из заданного списка успешно скрыты!");
    }

    // Восстановить из скрытия атрибут
    @PutMapping(value = "/recover_hidden_attribute/{attribute_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> recoverHiddenAttribute(@Valid @PathVariable(value = "attribute_id") @Min(0) long id) {

        Services.productAttributesService.recoverHiddenAttribute(id);

        return ResponseEntity.ok(String.format("Атрибут с id %d восстановлен из скрытия!", id));
    }

    // Восстановить из скрытия атрибуты
    @PutMapping(value = "/recover_hidden_attributes_list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> recoverHiddenAttributesList(@RequestBody() List<Long> attributesIdsList) {

        Services.productAttributesService.recoverHiddenAttributesList(attributesIdsList);

        return ResponseEntity.ok("Атрибуты из заданного списка восстановлены из скрытия!");
    }
}
