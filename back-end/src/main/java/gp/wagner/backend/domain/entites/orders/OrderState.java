package gp.wagner.backend.domain.entites.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

//Состояние заказа
@Entity
@Table(name = "order_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //Состояние
    @Column(name = "order_state")
    private String state;


}
