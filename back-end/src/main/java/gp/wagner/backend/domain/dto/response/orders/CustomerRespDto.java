package gp.wagner.backend.domain.dto.response.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.orders.Customer;
import jakarta.annotation.Nullable;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

//DTO для отправки на сторону клиента
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerRespDto {

    private Long id;

    // Посетитель, перешедший в разряд покупателя

    // ФИО отдельно
    @Nullable
    private String snp;

    //Фамилия
    @Nullable
    private String surname;

    //Имя
    @Nullable
    private String name;

    //Отчество
    @Nullable
    private String patronymic;

    //Email
    private String email;

    //Номер телефона
    @JsonProperty(value = "phone_number")
    private long phoneNumber;

    // Посетитель, с которого пришел заказ
    @JsonProperty(value = "visitor_finger_print")
    private String visitorFingerPrint;

    @JsonProperty(value = "created_at")
    private Instant createdAt;

    @JsonProperty(value = "updated_at")
    private Instant updatedAt;

    // Является ли покупатель зарегистрированным пользователем
    @JsonProperty(value = "is_registered")
    private Boolean isRegistered;

    // Количество заказов, совершенных данными покупателем
    @JsonProperty(value = "orders_count")
    private Long ordersCount;

    // Количество заказанных вариантов товаров во всех orders
    @JsonProperty(value = "ordered_units_count")
    private Integer orderedUnitsCount;

    // Средняя цена единицы товара в заказах покупателя
    @JsonProperty(value = "avg_unit_price")
    private Long avgUnitPrice;

    // Общая сумма всех заказов
    @JsonProperty(value = "orders_sum")
    private Integer ordersSum;

    public CustomerRespDto(Customer customer) {
        this.id = customer.getId();
        this.surname = customer.getSurname();
        this.name = customer.getName();
        this.patronymic = customer.getPatronymic();
        this.email = customer.getEmail();
        this.phoneNumber = customer.getPhoneNumber();
        this.visitorFingerPrint = customer.getVisitor().getFingerprint();
        this.createdAt = customer.getCreatedAt();
        this.updatedAt = customer.getUpdatedAt();
    }

    // Конструктор для выборки покупателей вместе со статистическими значениями (при выборке всех посетителей)
    public CustomerRespDto(Tuple tuple) {
        this.id = tuple.get(0, Long.class);
        this.email = tuple.get(1, String.class);
        this.snp = tuple.get(2, String.class);
        this.phoneNumber = tuple.get(3, Long.class);
        this.isRegistered = tuple.get(4, Boolean.class);
        this.createdAt = tuple.get(5, Instant.class);
        this.ordersCount = tuple.get(6, Long.class);
        this.orderedUnitsCount = tuple.get(7, Integer.class);
        this.avgUnitPrice = tuple.get(8, Double.class).longValue();
        this.ordersSum = tuple.get(9, Integer.class);
    }
}
