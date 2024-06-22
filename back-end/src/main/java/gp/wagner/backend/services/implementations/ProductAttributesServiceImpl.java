package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.ProductAttributeRequestDto;
import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.eav.ProductAttribute;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.domain.exceptions.suppliers.ProductAttributeNotFound;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.PaginationUtils;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.ProductAttributesRepository;
import gp.wagner.backend.services.interfaces.ProductAttributesService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductAttributesServiceImpl implements ProductAttributesService {

    @PersistenceContext
    private EntityManager entityManager;

    private ProductAttributesRepository paRepository;

    @Autowired
    public void setPaRepository(ProductAttributesRepository repository) {
        this.paRepository = repository;
    }

    @Override
    public ProductAttribute create(ProductAttributeRequestDto dto) {

        if (dto == null || dto.getName().isBlank())
            throw new ApiException("Создать атрибут для товаров не удалось. Dto задан некорректно!");

        float maxPriority = paRepository.getMaxPriority();

        ProductAttribute productAttribute = new ProductAttribute(null, dto.getName(), maxPriority + Constants.PRODUCT_ATTR_PRIORITY_INCREMENT,
                dto.getIsShown());

        // Задать принадлежность к категориям
        List<Category> categories;
        if (dto.getCategoriesIds() != null && !dto.getCategoriesIds().isEmpty()) {
            categories = Services.categoriesService.getByIdList(dto.getCategoriesIds());
            productAttribute.setCategories(categories);
        }
        else {
            categories = Services.categoriesService.getAllNotParentCategories();
            productAttribute.setCategories(categories);
        }


        return paRepository.saveAndFlush(productAttribute);

    }

    @Override
    public ProductAttribute update(ProductAttributeRequestDto dto) {
        if (dto == null || dto.getId() == null)
            throw new ApiException("Не получилось изменить Product Attribute. Задано некорректный DTO!");

        ProductAttribute attribute = paRepository.findById(dto.getId()).orElseThrow(new ProductAttributeNotFound(dto.getId()));

        attribute.setAttributeName(dto.getName());
        attribute.setIsShown(dto.getIsShown());

        // Изменить категории, к которым будет принадлежать атрибут
        if (dto.getCategoriesIds() != null && !dto.getCategoriesIds().isEmpty()){

            List<Category> newCategories = Services.categoriesService.getByIdList(dto.getCategoriesIds());

            // Оставить только те элементы, которые есть в списке найденных категорий по id из dto
            attribute.getCategories().retainAll(newCategories);

            for (Category category : newCategories) {
                // Если в текущем списке категорий нет категории, которая была задана в DTO
                if (!attribute.getCategories().contains(category))
                    attribute.getCategories().add(category);
            }

        }//if
        // Если категории вообще не были заданы, тогда очистить старые категории добавить все не родительские категории
        else {

            attribute.getCategories().clear();
            List<Category> categories = Services.categoriesService.getAllNotParentCategories();
            attribute.setCategories(categories);
        }

        return paRepository.saveAndFlush(attribute);

    }

    @Override
    public void updatePriority(long productAttrId, float priority) {
        if (productAttrId <= 0 || priority <= 0f)
            throw new ApiException("Не получилось изменить приоритет атрибута. Задано некорректное значение id или приоритета!");

        ProductAttribute attribute = paRepository.findById(productAttrId).orElseThrow(new ProductAttributeNotFound(productAttrId));

        attribute.setPriority(priority);

        paRepository.saveAndFlush(attribute);

    }

    @Override
    public void updatePriority(Map<Long, Float> attributesAndPriorities) {
        if (attributesAndPriorities == null || attributesAndPriorities.isEmpty())
            throw new ApiException("Не получилось изменить приоритеты нескольких атрибутов. Задана некорректна ассоциативная коллекция!");

        List<Long> idsList = attributesAndPriorities.keySet().stream().toList();
        List<ProductAttribute> foundAttributes = paRepository.getProductAttributesByIdsList(idsList)
                .orElseThrow(new ProductAttributeNotFound(idsList));

        // Изменить приоритеты вывода атрибутов
        foundAttributes.forEach(
                pa -> pa.setPriority(attributesAndPriorities.get(pa.getId()))
        );

        paRepository.saveAllAndFlush(foundAttributes);

    }

    @Override
    public void hideProductAttribute(long paId) {
        if (paId <= 0)
            throw new ApiException("Не получилось изменить приоритет атрибута. Задано некорректное значение id!");

        ProductAttribute attribute = paRepository.findById(paId).orElseThrow(new ProductAttributeNotFound(paId));

        if (!attribute.getIsShown())
            throw  new ApiException(String.format("Атрибут с id: %d уже скрыт!", paId));

        attribute.setIsShown(false);

        paRepository.saveAndFlush(attribute);
    }

    @Override
    public void hideProductAttributesList(List<Long> idsList) {
        if (idsList == null || idsList.isEmpty())
            throw new ApiException("Не получилось скрыть несколько атрибутов. Задана некорректна коллекция id!");

        List<ProductAttribute> foundAttributes = paRepository.getProductAttributesByIdsList(idsList).orElseThrow(new ProductAttributeNotFound(idsList));

        // Изменить приоритеты вывода атрибутов
        foundAttributes.forEach(
                pa -> pa.setIsShown(false)
        );

        paRepository.saveAllAndFlush(foundAttributes);
    }

    @Override
    public void recoverHiddenAttribute(long paId) {
        if (paId <= 0)
            throw new ApiException("Не получилось восстановить из скыртия атрибута. Задано некорректное значение id!");

        ProductAttribute attribute = paRepository.findById(paId).orElseThrow(new ProductAttributeNotFound(paId));

        if (attribute.getIsShown())
            throw  new ApiException(String.format("Атрибут с id: %d не был скрыт!", paId));

        attribute.setIsShown(true);

        paRepository.saveAndFlush(attribute);
    }

    @Override
    public void recoverHiddenAttributesList(List<Long> idsList) {
        if (idsList == null || idsList.isEmpty())
            throw new ApiException("Не получилось восстановить из скрытия несколько атрибутов. Задана некорректна коллекция id!");

        List<ProductAttribute> foundAttributes = paRepository.getProductAttributesByIdsList(idsList).orElseThrow(new ProductAttributeNotFound(idsList));

        // Изменить приоритеты вывода атрибутов
        foundAttributes.forEach(
                pa -> pa.setIsShown(true)
        );

        paRepository.saveAllAndFlush(foundAttributes);
    }

    @Override
    public Page<ProductAttribute> getByCategoryId(long categoryId, int pageNum, int dataOnPage, Sort sort) {

        if (pageNum > 0)
            pageNum -= 1;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<ProductAttribute> query = cb.createQuery(ProductAttribute.class);
        Root<ProductAttribute> root = query.from(ProductAttribute.class);

        Predicate predicate = cb.equal(root.get("categories").get("id"), categoryId);

        query.where(predicate);

        TypedQuery<ProductAttribute> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(dataOnPage);
        typedQuery.setFirstResult(pageNum*dataOnPage);

        List<ProductAttribute> attributes = typedQuery.getResultList();

        // Общее кол-во элементов с таким же ключевым словом и входящее в те же фильтра
        long elements = PaginationUtils.countProductAttributesByCategory(entityManager, categoryId);

        return new PageImpl<>(attributes, PageRequest.of(pageNum, dataOnPage), elements);
    }

    @Override
    public ProductAttribute getById(long paId) {
        return paRepository.findById(paId).orElseThrow(new ProductAttributeNotFound(paId));
    }

    @Override
    public Page<ProductAttribute> getAll(int pageNum, int dataOnPage) {

        if (pageNum > 0)
            pageNum -= 1;

        return paRepository.findAll(PageRequest.of(pageNum, dataOnPage));
    }
}
