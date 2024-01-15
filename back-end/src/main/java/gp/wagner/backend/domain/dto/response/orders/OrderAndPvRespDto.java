package gp.wagner.backend.domain.dto.response.orders;

import gp.wagner.backend.domain.dto.response.product_variant.ProductVariantPreviewRespDto;
import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

//DTO сущности таблицы многие ко многим для заказов
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAndPvRespDto {

    private long opvId;

    private long orderId;

    // Код заказа
    private long orderCode;

    // Статус заказа
    private long orderStateId;

    // Список заказываемых вариантов товаров
    private ProductVariantPreviewRespDto productVariantPreview;

    // Дата создания заказа
    private Date createdAt;

    public OrderAndPvRespDto(OrderAndProductVariant opv) {
        this.opvId = opv.getId();
        this.orderId = opv.getOrder().getId();
        this.orderCode = opv.getOrder().getCode();
        this.orderStateId = opv.getOrder().getOrderState().getId();
        this.productVariantPreview = new ProductVariantPreviewRespDto(opv.getProductVariant());
        this.createdAt = opv.getOrder().getOrderDate();
    }
}
