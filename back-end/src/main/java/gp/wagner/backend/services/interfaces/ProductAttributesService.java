package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.ProductAttributeRequestDto;
import gp.wagner.backend.domain.entities.eav.ProductAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;


public interface ProductAttributesService {

    // Добавление записи
    ProductAttribute create(ProductAttributeRequestDto productAttributeDto);

    // Изменение записи
    ProductAttribute update(ProductAttributeRequestDto productAttributeDto);

    // Изменение приоритета у одного атрибута
    void updatePriority(long productAttrId, float priority);

    // Изменение приоритета у нескольких атрибутов
    void updatePriority(Map<Long, Float> attributesAndPriorities);

    // Скрыть атрибут
    void hideProductAttribute(long paId);

    // Скрыть атрибуты
    void hideProductAttributesList(List<Long> idsList);

    // Восстановить атрибут из скрытия
    void recoverHiddenAttribute(long paId);

    // Восстановить атрибуты из скрытия
    void recoverHiddenAttributesList(List<Long> idsList);

    // Выборка атрибутов по id категории
    Page<ProductAttribute> getByCategoryId(long categoryId, int pageNum, int dataOnPage, Sort sort);

    // Выборка по id
    ProductAttribute getById(long paId);

    // Выборка всех записей
    Page<ProductAttribute> getAll(int pageNum, int dataOnPage);
}
