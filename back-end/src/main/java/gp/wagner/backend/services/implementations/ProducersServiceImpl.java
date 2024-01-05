package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.ProducersRepository;
import gp.wagner.backend.services.interfaces.ProducersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void update(Producer producer) {
        if (producer == null)
            throw new ApiException("Не получилось изменить объект Producer с id %d. Задано некорректное значение!");

        producersRepository.saveAndFlush(producer);
    }

    @Override
    public void deleteById(long id) {
        if(id <= 0)
            throw new ApiException(String.format("Не получилось удалить запись в Producer. Id: %d является некорректным!",id));

        producersRepository.deleteById(id);

    }

    @Override
    public List<Producer> getAll() {
        return producersRepository.findAll();
    }

    @Override
    public Producer getById(long id) {

        if(id <= 0)
            throw new ApiException(String.format("Не получилось найти Producer с Id: %d. Задано некорректное значение!",id));

        return producersRepository.getProducerById(id);
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
}
