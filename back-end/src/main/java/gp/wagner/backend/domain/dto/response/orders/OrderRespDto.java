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

//DTO для отправки на сторону клиента
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRespDto {

    private long id;

    // Код заказа
    private long code;

    // Покупатель
    private Customer customer;

    // Статус заказа
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

    //Сумма заказа
    private int sum;

    public OrderRespDto(Order order) {
        this.id = order.getId();
        this.code = order.getCode();
        this.customer = order.getCustomer();
        this.orderStateId = order.getOrderState().getId();
        //this.productVariantPreview = order.getOrderAndPVList().stream().map(opv -> new ProductVariantPreviewRespDto(opv.getProductVariant())).toList();
        this.productVariantsAndCount = order.getOrderAndPVList().stream().map(ProductVariantAndCount::new).toList();
        this.createdAt = order.getOrderDate();
        this.sum = order.getSum();
    }
}
