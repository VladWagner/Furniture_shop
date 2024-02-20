package gp.wagner.backend.domain.dto.response.orders;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.dto.response.product_variant.ProductVariantPreviewRespDto;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

//DTO для отправки на сторону клиента
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRespDto {

    @JsonProperty(index = 1)
    private long id;

    // Код заказа
    private long code;

    // Количество товаров в заказе
    @JsonProperty(value = "general_products_amount", index = 3)
    private int generalProductsAmount;

    //Сумма заказа
    @JsonProperty(index = 4)
    private int sum;

    // Покупатель
    @JsonProperty(value = "customer_dto")
    private CustomerRespDto customerDto;

    // Статус заказа
    @JsonProperty(value = "order_stateId", index = 5)
    private long orderStateId;

    // Список заказываемых вариантов товаров
    //private List<ProductVariantPreviewRespDto> productVariantPreview;

    private static class ProductVariantAndCount {
        public ProductVariantPreviewRespDto productVariantDto;

        public int count;

        public ProductVariantAndCount(OrderAndProductVariant opv) {
            this.productVariantDto = new ProductVariantPreviewRespDto(opv.getProductVariant());
            this.count = opv.getProductsAmount();
        }
    }

    private List<ProductVariantAndCount> productVariantsAndCount;

    // Дата создания заказа
    private Date createdAt;


    public OrderRespDto(Order order) {
        this.id = order.getId();
        this.code = order.getCode();
        this.customerDto = new CustomerRespDto(order.getCustomer());
        this.orderStateId = order.getOrderState().getId();
        this.productVariantsAndCount = order.getOrderAndPVList().stream().map(ProductVariantAndCount::new).toList();
        this.createdAt = order.getOrderDate();
        this.sum = order.getSum();
        this.generalProductsAmount = order.getGeneralProductsAmount();
    }
}
