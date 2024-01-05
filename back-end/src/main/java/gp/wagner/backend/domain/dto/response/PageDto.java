package gp.wagner.backend.domain.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Data
@NoArgsConstructor
@Getter
@Setter
public class PageDto<T> {

    // Основная коллекция
    List<T> collection;

    // Кол-во элементов на текущей странице
    int currentElementsAmount;

    // Кол-во страниц
    int generalPagesAmount;

    long generalElementsAmount;

    public PageDto(Page<T> page) {
        this.collection = page.getContent();
        this.currentElementsAmount = page.getNumberOfElements();
        this.generalPagesAmount = page.getTotalPages();
        this.generalElementsAmount = page.getTotalElements();
    }

    // Конструктор для страницы, которая содержит коллекцию в типе отличном от требуемого
    public PageDto(Page<?> page, Supplier<List<T>> collectionMapper) {
        this.collection = collectionMapper.get();
        this.currentElementsAmount = page.getNumberOfElements();
        this.generalPagesAmount = page.getTotalPages();
        this.generalElementsAmount = page.getTotalElements();
    }

    public PageDto(List<T> collection, int currentElementsAmount, int generalPagesAmount, long generalElementsAmount) {
        this.collection = collection;
        this.currentElementsAmount = currentElementsAmount;
        this.generalPagesAmount = generalPagesAmount;
        this.generalElementsAmount = generalElementsAmount;
    }
}

