package gp.wagner.backend.domain.dto.response;

import gp.wagner.backend.domain.dto.response.product_variant.ProductVariantPreviewRespDto;
import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.Order;
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

    private long id;

    // Покупатель
    private Customer customer;

    // Статус заказа
    //private OrderState orderState;
    private long orderStateId;

    // Список заказываемых вариантов товаров
    private List<ProductVariantPreviewRespDto> productVariantPreview;

    // Дата создания заказа
    private Date createdAt;

    //Сумма заказа
    private int sum;

    public OrderRespDto(Order order) {
        this.id = order.getId();
        this.customer = order.getCustomer();
        this.orderStateId = order.getOrderState().getId();
        this.productVariantPreview = order.getOrderAndPVList().stream().map(opv -> new ProductVariantPreviewRespDto(opv.getProductVariant())).toList();
        this.createdAt = order.getOrderDate();
        this.sum = order.getSum();
    }
}
