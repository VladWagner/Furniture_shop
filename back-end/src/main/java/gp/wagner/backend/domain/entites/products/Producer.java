package gp.wagner.backend.domain.entites.products;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.List;

//Производители
@Entity
@Table(name = "producers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Наименование
    @Column(name = "producer_name")
    private String producerName;

    //Товары, которые принадлежат данному производителю
    @OneToMany(mappedBy = "producer")
    @BatchSize(size = 20)
    private List<Product> products;
}