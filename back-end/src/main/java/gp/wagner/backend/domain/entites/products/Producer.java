package gp.wagner.backend.domain.entites.products;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.Date;
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

    // Флаг удаления элемента
    @Column(name = "deleted_at")
    private Date deletedAt;

    // Флаг вывода элемента
    @Column(name = "is_shown")
    private Boolean isShown;

    // Аватар компании (логотип и т.д.)
    @Column(name = "producer_img")
    private String producerLogo;

}