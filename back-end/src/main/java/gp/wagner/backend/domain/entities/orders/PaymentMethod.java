package gp.wagner.backend.domain.entities.orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Способ оплаты заказа
@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Состояние
    @Column(name = "method_name")
    private String methodName;

    public PaymentMethod(String methodName) {
        this.methodName = methodName;
    }
}
