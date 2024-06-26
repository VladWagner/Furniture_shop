package gp.wagner.backend.domain.dto.response.orders;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gp.wagner.backend.domain.dto.response.product_variants.ProductVariantPreviewRespDto;
import gp.wagner.backend.domain.entities.orders.Order;
import gp.wagner.backend.domain.entities.orders.OrderAndProductVariant;
import gp.wagner.backend.infrastructure.serializers.DateTimeJsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//DTO сущности таблицы многие ко многим для заказов
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAndPvRespDto {

    @JsonProperty("opv_id")
    private long opvId;

    @JsonProperty("order_id")
    private long orderId;

    // Код заказа
    @JsonProperty("order_code")
    private long orderCode;

    // Id покупателя
    @JsonProperty("customer_id")
    private long customerId;

    // Email покупателя
    @JsonProperty("customer_email")
    private String customerEmail;

    // Статус заказа
    @JsonProperty("order_state_id")
    private long orderStateId;

    // Сумма всего заказа - в этом dto задаётся только часть для конкретного варианта товара
    @JsonProperty("order_sum_general")
    private int orderSum;

    // Список заказываемых вариантов товаров - в записе таблице Order and product variant может быть задана только одна запись варианта товара
    @JsonProperty("product_variant_preview")
    private ProductVariantPreviewRespDto productVariantPreview;

    // Дата создания заказа
    @JsonProperty("created_at")
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date createdAt;

    public OrderAndPvRespDto(OrderAndProductVariant opv) {
        Order boundOrder = opv.getOrder();

        this.opvId = opv.getId();
        this.orderId = boundOrder.getId();
        this.orderCode = boundOrder.getCode();
        this.customerId = boundOrder.getCustomer().getId();
        this.customerEmail = boundOrder.getCustomer().getEmail();
        this.orderStateId = boundOrder.getOrderState().getId();
        this.orderSum = boundOrder.getSum();
        this.productVariantPreview = new ProductVariantPreviewRespDto(opv.getProductVariant());
        this.createdAt = boundOrder.getOrderDate();
    }
}
