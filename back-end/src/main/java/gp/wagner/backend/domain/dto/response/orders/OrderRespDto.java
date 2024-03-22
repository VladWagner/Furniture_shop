package gp.wagner.backend.domain.dto.response.orders;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gp.wagner.backend.domain.dto.response.product_variants.SimpleProductVariantRespDto;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.infrastructure.serializers.DateTimeJsonSerializer;
import jakarta.annotation.Nullable;
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
    @JsonProperty(value = "order_state_id", index = 5)
    private long orderStateId;

    // Способ оплаты
    @JsonProperty(value = "payment_method", index = 6)
    private Long paymentMethodId;

    // Способ оплаты
    @JsonProperty(index = 9)
    private String description;

    // Список заказываемых вариантов товаров

    private static class ProductVariantAndCount {
        public SimpleProductVariantRespDto productVariantDto;

        public int count;

        @JsonProperty("unit_price")
        public int unitPrice;

        public ProductVariantAndCount(OrderAndProductVariant opv) {
            this.productVariantDto = new SimpleProductVariantRespDto(opv.getProductVariant());
            this.count = opv.getProductsAmount();
            this.unitPrice = opv.getUnitPrice() != null ? opv.getUnitPrice() : 0;
        }
    }


    @JsonProperty(value = "productVariantsAndCount", index = 7)
    private List<ProductVariantAndCount> productVariantsAndCount;

    // Дата создания заказа
    @JsonProperty(value = "created_at", index = 8)
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date createdAt;


    public OrderRespDto(Order order) {
        this.id = order.getId();
        this.code = order.getCode();
        this.customerDto = order.getCustomer() != null ? new CustomerRespDto(order.getCustomer()) : null;
        this.orderStateId = order.getOrderState().getId();
        this.productVariantsAndCount = order.getOrderAndPVList().stream().map(ProductVariantAndCount::new).toList();
        this.createdAt = order.getOrderDate();
        this.sum = order.getSum();
        this.generalProductsAmount = order.getGeneralProductsAmount();
        this.paymentMethodId = order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null;
        this.description = order.getDescription();
    }
}
