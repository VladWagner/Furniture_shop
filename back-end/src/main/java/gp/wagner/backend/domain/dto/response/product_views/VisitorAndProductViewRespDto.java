package gp.wagner.backend.domain.dto.response.product_views;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.dto.response.VisitorRespDto;
import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.domain.entities.visits.ProductViews;
import gp.wagner.backend.domain.entities.visits.Visitor;
import gp.wagner.backend.infrastructure.SimpleTuple;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Посетители и товары, которые они смотрели
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VisitorAndProductViewRespDto {

    // Посетитель
    @JsonProperty(value = "visitor_dto", index = 1)
    private VisitorRespDto visitorDto;

    // Список просмотренных им товаров
    @JsonProperty(value = "products_views", index = 4)

    private List<ProductViewRespDto> productViewRespDtos;

    // Кол-во просмотренных товаров
    @JsonProperty(value = "views_count", index = 2)
    private int viewsCount;

    // Сумма просмотров товаров для заданного посетителя
    @JsonProperty(value = "views_sum", index = 3)
    private int viewsSum;

    public VisitorAndProductViewRespDto(VisitorRespDto visitorDto, List<ProductViewRespDto> productViewRespDtos) {
        this.visitorDto = visitorDto;
        this.productViewRespDtos = productViewRespDtos;
        this.viewsCount = productViewRespDtos.size();
        this.viewsSum = productViewRespDtos.stream()
                .mapToInt(ProductViewRespDto::getViewsCount)
                .sum();
    }

    public VisitorAndProductViewRespDto(Tuple tuple, Map<Long, Visitor> foundVisitors) {

        Visitor visitor = foundVisitors.get(tuple.get(0, Long.class));
        this.visitorDto = new VisitorRespDto(visitor);

        // Получить просмотры категорий для заданного посетителя
        this.productViewRespDtos = visitor.getProductViewsList().stream()
                .map(pv -> new ProductViewRespDto(pv.getProduct(), pv.getCount()))
                .collect(Collectors.toCollection(ArrayList::new));

        this.viewsCount = tuple.get(1, Long.class).intValue();
        this.viewsSum = tuple.get(2, Integer.class);
    }

    public VisitorAndProductViewRespDto(Tuple tuple, Map<Long, Visitor> foundVisitors, SimpleTuple<Integer, Integer> priceRange, Long categoryId) {

        Visitor visitor = foundVisitors.get(tuple.get(0, Long.class));
        this.visitorDto = new VisitorRespDto(visitor);

        // Получить просмотры категорий для заданного посетителя
        this.productViewRespDtos = visitor.getProductViewsList().stream()
                .filter(pv -> isPriceInRange(pv, categoryId, priceRange))
                .map(pv -> new ProductViewRespDto(pv.getProduct(), pv.getCount()))
                .toList();

        this.viewsCount = tuple.get(1, Long.class).intValue();
        this.viewsSum = tuple.get(2, Integer.class);
    }

    private boolean isPriceInRange(ProductViews pv, Long categoryId, SimpleTuple<Integer, Integer> priceRange){
        Product product = pv.getProduct();

        // Либо категория вообще не задана, либо категория равна заданной
        boolean result = categoryId == null || product.getCategory().getId().equals(categoryId);

        // Если диапазон цен задан и при этом проверка на равенство категории = true
                    /*result = priceRange != null && result ? product.getProductVariants()
                            .stream()
                            .anyMatch(v -> v.getPrice() >= priceRange.getValue1() && v.getPrice() <= priceRange.getValue2()) : result;*/
        ProductVariant variant = product.getProductVariants().get(0);
        result = priceRange != null && result ? variant.getPrice() >= priceRange.getValue1() && variant.getPrice() <= priceRange.getValue2() : result;

        return result;
    }

}
