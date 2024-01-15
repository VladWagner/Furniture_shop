package gp.wagner.backend.domain.entites.visits;

import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.Date;
import java.util.List;

//Посетитель
@Entity
@Table(name = "visitors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //ip пользователя
    @Column(name = "ip_address")
    private String ipAddress;

    //Отпечаток устройства пользователя
    @NotEmpty(message = "Fingerprint of browser cant be empty")
    @Column(name = "fingerprint")
    private String fingerprint;

    // Дата создания - дата первого просмотра
    @Column(name = "created_at")
    private Date createdAt;

    // Дата последнего посещения
    @Column(name = "last_visit_at")
    private Date lastVisit;

    // Просмотры товаров
    @OneToMany(mappedBy = "visitor")
    @BatchSize(size = 256)
    private List<ProductViews> productViewsList;

    public Visitor(Long id, String ipAddress, String fingerprint, Date lastVisit) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.fingerprint = fingerprint;
        this.lastVisit = lastVisit;
    }
}
