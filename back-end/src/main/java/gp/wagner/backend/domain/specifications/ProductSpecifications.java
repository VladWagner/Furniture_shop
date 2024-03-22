package gp.wagner.backend.domain.specifications;

import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterBlock;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDto;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.entites.eav.ProductAttribute;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.infrastructure.enums.FilterOperationsEnum;
import gp.wagner.backend.infrastructure.Utils;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import gp.wagner.backend.domain.entites.eav.AttributeValue;

import java.util.ArrayList;
import java.util.List;

//Спецификации для формирования hibernate'ом условий выборок из ДБ
public class ProductSpecifications {

    //Создать спецификации фильтрации ЧЕРЕЗ ПОДЗАПРОСЫ, которые hibernate будет использовать во время формирования запросов и предикатов
    public static List<Specification<Product>> createSubQueriesProductSpecifications(ProductFilterDtoContainer filtersDto) {

        //Спецификации для задания в CriteriaBuilder (уровень блоков фильтров)
        List<Specification<Product>> specifications = new ArrayList<>();

        // Выборка неудалённых и не скрытых элементов
        specifications.add(
                (root, query, cb) -> cb.and(
                                cb.or(
                                        cb.isNull(root.get("isDeleted")),
                                        cb.equal(root.get("isDeleted"), false)
                                    ) ,//or
                                cb.or(
                                        cb.isNull(root.get("showProduct")),
                                        cb.equal(root.get("showProduct"), true)
                                )
                        )// and
        );

        if (filtersDto.getProductFilterBlockList() == null)
            return specifications;

        //Список предикатов для каждой спецификации (уровень фильтров)
        List<Predicate> predicates = new ArrayList<>();

        //Пройти по каждому заданному блоку фильтров
        for (ProductFilterBlock filterBlock : filtersDto.getProductFilterBlockList()) {

            //Отдельная спецификация под каждый блок - задаётся в общий список specifications
            Specification<Product> specification = filterBlock.getProductFilters().size() == 0 ?
                    null :
                    (root, criteriaQuery, cb) -> {
                        //Очистка списка предикатов для каждого блока фильтров
                        predicates.clear();

                        Predicate subQueryPredicate = null;

                        //Создание предикатов фильтрации по каждому фильтру - внутри всё
                        for (ProductFilterDto dto : filterBlock.getProductFilters()) {

                            // Подзапрос к таблице значений атрибутов под конкретное значение фильтра
                            Subquery<Long> subQueryAv = criteriaQuery.subquery(Long.class);

                            Root<Product> subRoot = subQueryAv.from(Product.class);

                            // Присоединить таблицу значений для конкретного товара
                            Join<Product, AttributeValue> subProdAttrValueJoin = subRoot.join("attributeValues");

                            // Соединение с таблицей самих атрибутов для проверки id
                            Path<ProductAttribute> subProductAttrPath = subProdAttrValueJoin.get("attribute");

                            // Далее для каждого условия и атрибута будет формироваться свой подзапрос

                            // Выбор заданной операции для каждого параметра фильтра
                            if (dto.getOperation().equals(FilterOperationsEnum.EQUALS.getValue())) {

                                Integer dataInt = Utils.TryParseInt(dto.getValue());

                                subQueryPredicate = cb.and(
                                                //Поиск нужной характеристики - id в фильтре с id в таблице атрибутов
                                                cb.equal(subProductAttrPath.get("id"), dto.getAttributeId()),
                                                //Поиск и сравнение значения характеристики (пока по двум базовым полям)
                                                dataInt != null ? cb.equal(subProdAttrValueJoin.get("intValue"), dataInt) :
                                                        cb.equal(subProdAttrValueJoin.get("strValue"), dto.getValue())
                                        );//cb.and;
                            }
                            // end-if
                            // Если значение должно быть >= заданного
                            else if (dto.getOperation().equals(FilterOperationsEnum.GREATER_THAN_EQUAL.getValue()))
                                subQueryPredicate = cb.and(
                                                cb.equal(subProductAttrPath.get("id"), dto.getAttributeId()),
                                                cb.ge(subProdAttrValueJoin.get("intValue"), Utils.TryParseInt(dto.getValue()))
                                        );//cb.and

                                //end-else if

                            // Если значение должно быть <= заданного
                            else if (dto.getOperation().equals(FilterOperationsEnum.LESS_THAN_EQUAL.getValue()))
                                subQueryPredicate = cb.and(
                                                cb.equal(subProductAttrPath.get("id"), dto.getAttributeId()),
                                                cb.le(subProdAttrValueJoin.get("intValue"), Utils.TryParseInt(dto.getValue()))
                                        );//cb.and
                                //end-else if

                            // Если значение должно быть между заданными (целочисленными)
                            else if (dto.getOperation().equals(FilterOperationsEnum.BETWEEN.getValue())) {

                                //Попытка вытащить целочисленные значения из строки
                                String[] values = dto.getValue().split("[-_–—|]");

                                //Todo: я бы ещё продумал, а могут ли здесь быть вещественные значении
                                Integer val1 = Utils.TryParseInt(values[0]);
                                Integer val2 = Utils.TryParseInt(values[1]);

                                //Если значений для фильтрации нет, тогда этот фильтр игнорируем и переходим к следующему
                                if (val1 == null || val2 == null)
                                    continue;

                                subQueryPredicate = cb.and(
                                                cb.equal(subProductAttrPath.get("id"), dto.getAttributeId()),
                                                cb.between(subProdAttrValueJoin.get("intValue"), val1, val2)
                                        );//cb.and
                            }
                            //end-else if

                            // Сформировать одно из условий в where из результатов подзапроса (id текущего товара должен быть в результате подзапроса)
                            if (subQueryPredicate != null) {
                                subQueryAv.select(subRoot.get("id")).where(subQueryPredicate);
                                predicates.add(cb.in(root.get("id")).value(subQueryAv));
                            }

                        }//for

                        // Выбор внутреннего объединения условий
                        if (filterBlock.getInnerRule().equalsIgnoreCase(FilterOperationsEnum.OR.getValue()))
                            return cb.or(predicates.toArray(new Predicate[0]));
                        else
                            return cb.and(predicates.toArray(new Predicate[0]));
                    }; //Анонимное создание объекта спецификации, которое будет происходить уже при формировании предикатов в сервисе -
                       //каждый раз будет вызываться метод (root, criteriaQuery, cb) -> {}, в котором уже будут замкнуты нужные нам значения

            specifications.add(specification);

        }//for

        return specifications;
    }

}