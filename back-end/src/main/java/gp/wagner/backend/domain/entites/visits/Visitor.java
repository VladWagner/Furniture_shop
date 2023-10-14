package gp.wagner.backend.domain.entites.visits;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

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
}
