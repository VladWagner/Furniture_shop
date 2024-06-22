package gp.wagner.backend.domain.entities.categories;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

//Повторяющиеся категории
@Entity
@Table(name = "subcategories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepeatingCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Наименование категории
    @Column(name = "sub_name")
    private String name;

    // Изображение повторяющейся категории
    @Column(name = "image")
    private String image;

    public RepeatingCategory(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
