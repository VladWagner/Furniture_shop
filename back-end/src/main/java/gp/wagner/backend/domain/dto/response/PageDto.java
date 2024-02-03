package gp.wagner.backend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    // Кол-во элементов на текущей странице
    @JsonProperty("current_elements_amount")
    int currentElementsAmount;

    // Кол-во страниц
    @JsonProperty("general_pages_amount")
    int generalPagesAmount;

    @JsonProperty("general_elements_amount")
    long generalElementsAmount;

    @JsonProperty("current_page")
    int currentPage;

    // Основная коллекция
    @JsonProperty("collection")
    List<T> collection;

    public PageDto(Page<T> page) {
        this.collection = page.getContent();
        this.currentElementsAmount = page.getNumberOfElements();
        this.generalPagesAmount = page.getTotalPages();
        this.generalElementsAmount = page.getTotalElements();
        this.currentPage = page.getNumber()+1;
    }

    // Конструктор для страницы, которая содержит коллекцию в типе отличном от требуемого из Generic
    public PageDto(Page<?> page, Supplier<List<T>> collectionMapper) {
        this.collection = collectionMapper.get();
        this.currentElementsAmount = page.getNumberOfElements();
        this.generalPagesAmount = page.getTotalPages();
        this.generalElementsAmount = page.getTotalElements();
        this.currentPage = page.getNumber()+1;
    }

    public PageDto(List<T> collection, int currentElementsAmount, int generalPagesAmount, long generalElementsAmount, int currentPage) {
        this.collection = collection;
        this.currentElementsAmount = currentElementsAmount;
        this.generalPagesAmount = generalPagesAmount;
        this.generalElementsAmount = generalElementsAmount;
        this.currentPage = currentPage;
    }
}



