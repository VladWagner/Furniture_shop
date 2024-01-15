package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.ProducerRequestDto;
import gp.wagner.backend.domain.entites.products.Producer;
import org.springframework.data.domain.Page;

import java.util.List;


public interface ProducersService {

    //Добавление записи
    void create(Producer producer);

    Producer create(ProducerRequestDto dto, String imageUri);

    //Изменение записи
    void update(Producer producer);
    void update(ProducerRequestDto dto, String imageUri);

    // Удалить по id
    void deleteById(long id);

    // Восстановить производителя. Параметры: id и восстанавливать ли связанные записи
    void recoverById(long id, boolean recoverHeirs);

    // Скрыть производителя по id + скрыть все его товары и варианты
    void hideById(long id);

    // Восстановить из скрытия по id производителя + восстановить все его товары и варианты
    void recoverHiddenById(long id, boolean recoverHeirs);

    //Выборка всех записей
    Page<Producer> getAll(int pageNum, int limit);

    //Выборка записи под id
    Producer getById(long id);

    //Выборка производителей в определённой категории
    List<Producer> getProducersByCategory(long categoryId);

    //Выборка производителей в нескольких категориях
    List<Producer> getProducersInCategories(List<Long> categoriesIds);

    List<Producer> getProducersByProductKeyword(String keyword);
}
