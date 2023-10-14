package gp.wagner.backend.domain.specifications;

import gp.wagner.backend.domain.dto.request.filters.ProductFilterBlock;
import gp.wagner.backend.domain.dto.request.filters.ProductFilterDto;
import gp.wagner.backend.domain.dto.request.filters.ProductFilterDtoContainer;
import gp.wagner.backend.domain.entites.eav.ProductAttribute;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.infrastructure.FilterOperations;
import gp.wagner.backend.infrastructure.Utils;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import gp.wagner.backend.domain.entites.eav.AttributeValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Спецификации для формирования hibernate'ом условий выборок из ДБ
public class ProductSpecifications {

    //Спецификация фильтрации
    public static List<Specification<Product>> createProductSpecification(ProductFilterDtoContainer filtersDto) {

        List<Predicate> predicates = new ArrayList<>();

        Specification<Product> specificationAnd = filtersDto.getProductFilterDtoListAnd().size() == 0 ?
                null :
                (root, criteriaQuery, cb) -> {


                    //Создание предикатов фильтрации по каждому
                    for (ProductFilterDto dto : filtersDto.getProductFilterDtoListAnd()) {

                        //Присоединить таблицу значений для конкретного товара
                        Join<Product, AttributeValue> prodAttrValueJoin = root.join("attributeValues");

                        //Соединение с таблицей названий атрибутов
                        Path<ProductAttribute> productAttribute = prodAttrValueJoin.get("attribute");

                        //Выбор заданной операции для каждого параметра фильтра
                        //В чём идея - добаляем предикат, где id характеристик = требуемому и при этом значение так же ==
                        if (dto.getOperation().equals(FilterOperations.EQUALS.getValue()))
                            predicates.add(cb.and(
                                            //Поиск нужной характеристики
                                            cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                            //Поиск и сравнение значения характеристики
                                            cb.or(
                                                    cb.equal(prodAttrValueJoin.get("strValue"), dto.getValue()),
                                                    cb.equal(prodAttrValueJoin.get("intValue").as(Integer.class), Utils.TryParseInt(dto.getValue()))
                                                    //cb.equal(prodAttrValueJoin.get("floatValue"),dto.value),
                                                    //cb.equal(prodAttrValueJoin.get("boolValue"),dto.value)
                                            )//cb.or
                                    )//cb.and
                            );
                            //end-if
                            //Если значение должно быть >= заданного
                        else if (dto.getOperation().equals(FilterOperations.GREATER_THAN_EQUAL.getValue()))
                            predicates.add(cb.and(
                                            //Поиск нужной характеристики
                                            cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                            //Поиск и сравнение значения характеристики
                                            cb.ge(prodAttrValueJoin.get("intValue").as(Integer.class), Utils.TryParseInt(dto.getValue()))
                                    )//cb.and
                            );
                            //end-else if

                            //Если значение должно быть <= заданного
                        else if (dto.getOperation().equals(FilterOperations.LESS_THAN_EQUAL.getValue()))
                            predicates.add(cb.and(
                                            //Поиск нужной характеристики
                                            cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                            //Поиск и сравнение значения характеристики
                                            cb.le(prodAttrValueJoin.get("intValue").as(Integer.class), Utils.TryParseInt(dto.getValue()))
                                    )//cb.and
                            );
                        //end-else if

                    }//for


                    return cb.and(predicates.toArray(new Predicate[0]));
                };

        Specification<Product> specificationOr = filtersDto.getProductFilterDtoListOr().size() == 0 ?
                null :
                ((root, criteriaQuery, cb) -> {

                    if (predicates.size() > 0)
                        predicates.clear();

                    //Создание предикатов фильтрации по каждому
                    for (ProductFilterDto dto : filtersDto.getProductFilterDtoListOr()) {

                        //Присоединить таблицу значений для конкретного товара
                        Join<Product, AttributeValue> prodAttrValueJoin = root.join("attributeValues");

                        //Соединение с таблицей названий атрибутов
                        Path<ProductAttribute> productAttribute = prodAttrValueJoin.get("attribute");

                        //Выбор заданной операции для каждого параметра фильтра
                        //В чём идея - добаляем предикат, где id характеристик = требуемому и при этом значение так же ==
                        if (dto.getOperation().equals(FilterOperations.EQUALS.getValue()))
                            predicates.add(cb.and(
                                            //Поиск нужной характеристики
                                            cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                            //Поиск и сравнение значения характеристики
                                            cb.or(
                                                    cb.equal(prodAttrValueJoin.get("strValue"), dto.getValue()),
                                                    cb.equal(prodAttrValueJoin.get("intValue").as(Integer.class), Utils.TryParseInt(dto.getValue()))
                                            )//cb.or
                                    )//cb.and
                            );
                            //end-if
                            //Если значение должно быть >= заданного
                        else if (dto.getOperation().equals(FilterOperations.GREATER_THAN_EQUAL.getValue()))
                            predicates.add(cb.and(
                                            //Поиск нужной характеристики
                                            cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                            //Поиск и сравнение значения характеристики
                                            cb.ge(prodAttrValueJoin.get("intValue").as(Integer.class), Utils.TryParseInt(dto.getValue()))
                                    )//cb.and
                            );
                            //end-else if

                            //Если значение должно быть <= заданного
                        else if (dto.getOperation().equals(FilterOperations.LESS_THAN_EQUAL.getValue()))
                            predicates.add(cb.and(
                                            //Поиск нужной характеристики
                                            cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                            //Поиск и сравнение значения характеристики
                                            cb.le(prodAttrValueJoin.get("intValue").as(Integer.class), Utils.TryParseInt(dto.getValue()))
                                    )//cb.and
                            );
                        //end-else if

                    }//for

                    return cb.or(predicates.toArray(new Predicate[0]));
                });

        return new ArrayList<>(Arrays.asList(specificationAnd, specificationOr));
    }

    //Создать спецификации фильтрации, которые hibernate будет использовать о время формирования запросов и предикатов
    public static List<Specification<Product>> createNestedProductSpecifications(ProductFilterDtoContainer filtersDto) {

        //Список предикатов для каждой спецификации (уровень фильтров)
        List<Predicate> predicates = new ArrayList<>();

        //Спецификации для задания в CriteriaBuilder (уровень блоков фильтров)
        List<Specification<Product>> specifications = new ArrayList<>();

        //Пройти по каждому заданному блоку фильтров
        for (ProductFilterBlock filterBlock : filtersDto.getProductFilterBlockList()) {

            //Отдельная спецификация под каждый блок - задаётся в общий список specifications
            Specification<Product> specification = filterBlock.getProductFilters().size() == 0 ?
                    null :
                    (root, criteriaQuery, cb) -> {
                        //Очистка списка предикатов для каждого блока фильтров
                        predicates.clear();

                        //Создание предикатов фильтрации по каждому фильтру - внутри всё
                        for (ProductFilterDto dto : filterBlock.getProductFilters()) {

                            //Присоединить таблицу значений для конкретного товара (в attributeValues есть внешний ключ на сущность Product)
                            Join<Product, AttributeValue> prodAttrValueJoin = root.join("attributeValues");

                            //Соединение с таблицей названий атрибутов
                            Path<ProductAttribute> productAttribute = prodAttrValueJoin.get("attribute");

                            //Выбор заданной операции для каждого параметра фильтра
                            if (dto.getOperation().equals(FilterOperations.EQUALS.getValue()))
                                predicates.add(cb.and(
                                                //Поиск нужной характеристики - id в фильтре с id d таблице атрибутов
                                                cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                                //Поиск и сравнение значения характеристики (пока по двум базовым полям)
                                                //Т.е. здесь либо int значение будет = требуемому, либо String
                                                cb.or(
                                                        cb.equal(prodAttrValueJoin.get("strValue"), dto.getValue()),
                                                        cb.equal(prodAttrValueJoin.get("intValue"), Utils.TryParseInt(dto.getValue()))
                                                )//cb.or
                                        )//cb.and
                                );
                                //end-if
                            //Если значение должно быть >= заданного
                            else if (dto.getOperation().equals(FilterOperations.GREATER_THAN_EQUAL.getValue()))
                                predicates.add(cb.and(
                                                //Поиск нужной характеристики
                                                cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                                //Поиск и сравнение значения характеристики
                                                cb.ge(prodAttrValueJoin.get("intValue"), Utils.TryParseInt(dto.getValue()))
                                        )//cb.and
                                );
                                //end-else if

                            //Если значение должно быть <= заданного
                            else if (dto.getOperation().equals(FilterOperations.LESS_THAN_EQUAL.getValue()))
                                predicates.add(cb.and(
                                                //Поиск нужной характеристики
                                                cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                                //Поиск и сравнение значения характеристики
                                                cb.le(prodAttrValueJoin.get("intValue"), Utils.TryParseInt(dto.getValue()))
                                        )//cb.and
                                );
                            //end-else if

                            //Если значение должно быть между заданными (целочисленными)
                            else if (dto.getOperation().equals(FilterOperations.BETWEEN.getValue())) {


                                //Попытка вытащить целочисленные значения из строки
                                String[] values = dto.getValue().split("[-_–—|]");

                                //Todo: я бы ещё продумал, а могут ли здесь быть вещественные значении
                                Integer val1 = Utils.TryParseInt(values[0]);
                                Integer val2 = Utils.TryParseInt(values[1]);

                                //Если значений для фильтрации нет, тогда этот фильтр игнорируем и переходим к следующему
                                if (val1 == null || val2 == null)
                                    continue;

                                predicates.add(cb.and(
                                                //Поиск нужной характеристики
                                                cb.equal(productAttribute.get("id"), dto.getAttributeId()),
                                                //Поиск и сравнение значения характеристики
                                                cb.between(prodAttrValueJoin.get("intValue"), val1, val2)
                                        )//cb.and
                                );
                            }
                            //end-else if

                        }//for

                        //Выбор внутреннего объединения условий
                        if (filterBlock.getInnerRule().equalsIgnoreCase(FilterOperations.OR.getValue()))
                            return cb.or(predicates.toArray(new Predicate[0]));
                        else
                            return cb.and(predicates.toArray(new Predicate[0]));
                    };//Анонимное создание объекта спецификации, которое будет происходить уже при формировании предикатов в сервисе -
                      //каждый раз будет вызываться метод (root, criteriaQuery, cb) -> {}, в котором уже будут замкнуты нужные нам значения

            specifications.add(specification);

        }//for

        return specifications;
    }

}