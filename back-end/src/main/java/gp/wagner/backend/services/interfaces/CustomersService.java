package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.CustomerRequestDto;
import gp.wagner.backend.domain.dto.request.filters.CustomersFilterRequestDto;
import gp.wagner.backend.domain.dto.response.filters.CustomersFilterValuesDto;
import gp.wagner.backend.domain.entities.orders.Customer;
import gp.wagner.backend.infrastructure.enums.sorting.CustomersSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;


public interface CustomersService {

    Page<Tuple> getAllWithStat(int pageNum, int limit, CustomersFilterRequestDto filterDto, CustomersSortEnum sortEnum, GeneralSortEnum sortType);

    CustomersFilterValuesDto getFilterValues();

    // Добавление покупателя
    void create(Customer customer);

    Customer create(CustomerRequestDto customerDto, String fingerprint, String ip);

    Customer getCustomerByEmailOrId(String email, Long id);

    //Изменение записи
    void update(Customer customer);
    Customer update(CustomerRequestDto customer);

    //Выборка записи под id
    Customer getById(Long id);

    // Найти покупателя по email
    Customer getCustomerByEmail(String email);

}
