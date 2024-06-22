package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.ProducerRequestDto;
import gp.wagner.backend.domain.entities.products.Producer;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.SortingUtils;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.ProducersSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.ProducersRepository;
import gp.wagner.backend.services.interfaces.ProducersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ProducersServiceImpl implements ProducersService {

    // Репозиторий
    private ProducersRepository producersRepository;

    // Получение бина репозитория
    @Autowired
    public void setProducersRepository(ProducersRepository producersRepository) {
        this.producersRepository = producersRepository;
    }

    @Override
    public void create(Producer producer) {
        if (producer == null)
            throw new ApiException("Не получилось создать объект Producer. Задано некорректное значение!");

        producersRepository.saveAndFlush(producer);
    }

    @Override
    public Producer create(ProducerRequestDto dto, String imageUri) {

        if (dto == null)
            throw new ApiException("Не получилось создать объект Producer. Dto задан некорректно!");

        Producer producer = new Producer(null, dto.getProducerName(), null, null, dto.getIsShown(),
                imageUri.isEmpty() ? /*Constants.EMPTY_IMAGE.toString()*/Services.fileManageService.getFilesPaths().emptyImagePath().toString() : imageUri);

        return producersRepository.saveAndFlush(producer);

    }

    @Override
    public void update(Producer producer) {
        if (producer == null)
            throw new ApiException("Не получилось изменить объект Producer с id %d. Задано некорректное значение!");

        Producer oldProducer = producersRepository.getProducerById(producer.getId())
                .orElseThrow(() -> new ApiException(String.format("Producer с Id: %d не найден!",producer.getId())));

        boolean oldShownValue = oldProducer.getIsShown();
        Date oldDeletedAtValue = oldProducer.getDeletedAt();

        producersRepository.saveAndFlush(producer);

        // Если производитель был скрыт
        if (!producer.getIsShown() && oldShownValue)
            Services.productsService.hideByProducer(producer);
        // Если производителя восстановили из скрытия
        else if (!oldShownValue && producer.getIsShown())
            Services.productsService.recoverHiddenByProducer(oldProducer);

        // Если производитель был удалён
        if(oldDeletedAtValue == null && producer.getDeletedAt() != null)
            Services.productsService.deleteByProducerId(oldProducer);

    }

    @Override
    public void update(ProducerRequestDto dto, String imageUri) {

        if (dto == null || dto.getId() == null)
            throw new ApiException("Не получилось изменить объект Producer. Dto задан некорректно!");

        Producer foundProducer = producersRepository.getProducerById(dto.getId())
                .orElseThrow(() -> new ApiException(String.format("Producer с Id: %d не найден!",dto.getId())));

        boolean oldShownValue = foundProducer.getIsShown();
        Date oldDeletedAtValue = foundProducer.getDeletedAt();

        foundProducer.setProducerName(dto.getProducerName());
        foundProducer.setIsShown(dto.getIsShown());
        foundProducer.setDeletedAt(dto.getDeleted() ? new Date() : foundProducer.getDeletedAt());
        foundProducer.setProducerLogo(imageUri == null || imageUri.isBlank() ? foundProducer.getProducerLogo() : imageUri);

        producersRepository.saveAndFlush(foundProducer);

        // Если производитель был скрыт
        if (!foundProducer.getIsShown() && oldShownValue)
            Services.productsService.hideByProducer(foundProducer);
        // Если производителя восстановили из скрытия и задан флаг восстановления всех связанных сущностей
        else if (dto.getIsDisclosed() && dto.getDiscloseHeirs())
            Services.productsService.recoverHiddenByProducer(foundProducer);

        // Если производитель был удалён
        if(oldDeletedAtValue == null && foundProducer.getDeletedAt() != null)
            Services.productsService.deleteByProducerId(foundProducer);

    }

    @Override
    public void deleteById(long id) {

        Producer foundProducer = producersRepository.findById(id).orElseThrow(() -> new ApiException(String.format("Не удалось найти производителя с id = %d!",id)));

        if (foundProducer.getDeletedAt() != null)
            throw  new ApiException(String.format("Производитель с id: %d уже удалён!", id));

        foundProducer.setDeletedAt(new Date());

        Services.productsService.deleteByProducerId(foundProducer);

        producersRepository.saveAndFlush(foundProducer);

    }

    @Override
    public void recoverById(long id, boolean recoverHeirs) {

        Producer foundProducer = producersRepository.findById(id).orElseThrow(() -> new ApiException(String.format("Не удалось найти производителя с id = %d!",id)));

        if (foundProducer.getDeletedAt() == null)
            throw  new ApiException(String.format("Производитель с id: %d не был удалён!", id));

        foundProducer.setDeletedAt(null);

        if (recoverHeirs)
            Services.productsService.recoverDeletedByProducerId(foundProducer);

        producersRepository.saveAndFlush(foundProducer);

    }

    @Override
    public void hideById(long id) {

        Producer foundProducer = producersRepository.findById(id)
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти производителя с id = %d!",id)));

        if (!foundProducer.getIsShown())
            throw  new ApiException(String.format("Производитель с id: %d уже скрыт!", id));

        foundProducer.setIsShown(false);

        producersRepository.saveAndFlush(foundProducer);

        // Скрыть все товары, варианты и изменить корзины
        Services.productsService.hideByProducer(foundProducer);

    }

    @Override
    public void recoverHiddenById(long id, boolean recoverHeirs) {

        Producer foundProducer = producersRepository.findById(id)
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти производителя с id = %d!",id)));

        if (foundProducer.getIsShown())
            throw  new ApiException(String.format("Производитель с id: %d не был скрыт!", id));

        foundProducer.setIsShown(true);

        producersRepository.saveAndFlush(foundProducer);

        // Восстановить из скрытия все товары, варианты и изменить корзины
        if (recoverHeirs)
            Services.productsService.recoverHiddenByProducer(foundProducer);

    }

    @Override
    public Page<Producer> getAll(int pageNum, int limit, ProducersSortEnum sortEnum, GeneralSortEnum sortType) {
        Sort sort = SortingUtils.createSortForProducersSelection(sortEnum, sortType);
        return producersRepository.findAll(PageRequest.of(pageNum-1, limit, sort));
    }

    @Override
    public Producer getById(long id) {

        if(id <= 0)
            throw new ApiException(String.format("Не получилось найти Producer с Id: %d. Задано некорректное значение!",id));

        return producersRepository.getProducerById(id).orElseThrow(() -> new ApiException(String.format("Producer с Id: %d не найден!",id)));
    }

    @Override
    public List<Producer> getProducersByCategory(long categoryId) {

        if(categoryId <= 0 || Services.categoriesService.getById(categoryId) == null)
            //throw new ApiException(String.format("Не получилось найти Producer в категории с Id: %d. Задано некорректное значение!",categoryId));
            return producersRepository.findAll();

        return producersRepository.getProducersInCategory(categoryId);
    }

    @Override
    public List<Producer> getProducersInCategories(List<Long> categoriesIds) {

        return categoriesIds != null && categoriesIds.size() > 0 ?
                producersRepository.getProducersInCategories(categoriesIds) :
                producersRepository.findAll();
    }

    @Override
    public List<Producer> getProducersByProductKeyword(String keyword) {

        return producersRepository.getProducersByProductKeyword(keyword);
    }

    @Override
    public Page<Producer> getAllDeleted(int pageNum, int limit, ProducersSortEnum sortEnum, GeneralSortEnum sortType) {

        if (pageNum > 0)
            pageNum -= 1;

        Sort sort = SortingUtils.createSortForProducersSelection(sortEnum, sortType);
        return producersRepository.getDeletedProducers(PageRequest.of(pageNum, limit, sort));
    }
}
