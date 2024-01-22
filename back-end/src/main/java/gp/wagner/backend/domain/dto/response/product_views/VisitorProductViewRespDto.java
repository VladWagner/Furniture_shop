package gp.wagner.backend.domain.dto.response.product_views;

import gp.wagner.backend.domain.dto.response.VisitorRespDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.visits.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

//Посетители и товары, которые они смотрели
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VisitorProductViewRespDto {

    // Посетитель
    private VisitorRespDto visitorDto;

    // Список просмотренных им товаров
    private List<ProductViewRespDto> productViewRespDtos;

}
