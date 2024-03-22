package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.CustomerRequestDto;
import gp.wagner.backend.domain.dto.request.filters.CustomersFilterRequestDto;
import gp.wagner.backend.domain.dto.response.filters.CustomersFilterValuesDto;
import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.PaginationUtils;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.SortingUtils;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.infrastructure.enums.sorting.CustomersSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.CustomersRepository;
import gp.wagner.backend.services.interfaces.CustomersService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.apache.commons.math3.analysis.function.Exp;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomersServiceImpl implements CustomersService {

    @PersistenceContext
    private EntityManager entityManager;

    // Репозиторий покупателей
    private CustomersRepository customersRepository;

    @Autowired
    public void setCustomersRepository(CustomersRepository repository) {
        this.customersRepository = repository;
    }
    @Override
    public Page<Tuple> getAllWithStat(int pageNum, int limit, CustomersFilterRequestDto filterDto, CustomersSortEnum sortEnum, GeneralSortEnum sortType) {
        if (pageNum > 0)
            pageNum -= 1;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<Customer> root = query.from(Customer.class);
        Join<Customer, User> userJoin = root.join("user", JoinType.LEFT);


        //region Подзапрос 1 - общее кол-во заказов для каждого покупателя
        Subquery<Long> ordersCountSubquery = query.subquery(Long.class);
        Root<Order> ordersCountRoot = ordersCountSubquery.from(Order.class);
        ordersCountSubquery.where(cb.equal(ordersCountRoot.get("customer").get("id"), root.get("id")));

        //endregion

        Expression<Long> ordersCountExp = ordersCountSubquery.select(cb.count(ordersCountRoot.get("id"))).getSelection();

        //region Подзапрос 2 - кол-во заказанных товаров во всех orders покупателя
        Subquery<Integer> ordersUnitsCountSubquery = query.subquery(Integer.class);
        Root<Order> ordersUnitsCountRoot = ordersUnitsCountSubquery.from(Order.class);
        ordersUnitsCountSubquery.where(cb.equal(ordersUnitsCountRoot.get("customer").get("id"), root.get("id")));


        //endregion

        Expression<Integer> ordersUnitsCountExp = ordersUnitsCountSubquery.select(
                cb.sum(ordersUnitsCountRoot.get("generalProductsAmount"))
        );

        //region Подзапрос 3 - средняя цена товара в заказах каждого покупателя
        Subquery<Double> avgOrderedUnitPriceSubquery = query.subquery(Double.class);
        Root<Order> avgOrderedUnitPriceRoot = avgOrderedUnitPriceSubquery.from(Order.class);
        avgOrderedUnitPriceSubquery.where(cb.equal(avgOrderedUnitPriceRoot.get("customer").get("id"), root.get("id")));


        //endregion

        Expression<Double> avgUnitPriceExp = avgOrderedUnitPriceSubquery.select(
                cb.avg(cb.quot(avgOrderedUnitPriceRoot.get("sum"), avgOrderedUnitPriceRoot.get("generalProductsAmount")))
        );

        //region Подзапрос 4 - средняя цена товара в заказах каждого покупателя
        Subquery<Integer> ordersSumsSubquery = query.subquery(Integer.class);
        Root<Order> ordedrsSumsRoot = ordersSumsSubquery.from(Order.class);
        ordersSumsSubquery.where(cb.equal(ordedrsSumsRoot.get("customer").get("id"), root.get("id")));


        //endregion

        Expression<Integer> ordersSumsExp = ordersSumsSubquery.select(
                cb.sum(ordedrsSumsRoot.get("sum"))
        );

        Expression<String> customerSnpExp = ServicesUtils.customerSnpExpression(cb, root);
        Expression<Boolean> isRegisteredExp = cb.isNotNull(userJoin);

        // Сформировать предикаты по спецификации из фильтра
        Predicate predicate = filterDto.getId() == null ? cb.and(ServicesUtils.collectCustomersPredicates(cb, root, filterDto, isRegisteredExp,
                ordersCountExp, ordersUnitsCountExp, ordersSumsExp, avgUnitPriceExp).toArray(new Predicate[0])) :
                cb.equal(root.get("id"), filterDto.getId());

        query.where(predicate).multiselect(
                root.get("id"),
                root.get("email"),
                customerSnpExp,
                root.get("phoneNumber"),
                isRegisteredExp,
                root.get("createdAt"),
                ordersCountExp,
                ordersUnitsCountExp,
                avgUnitPriceExp,
                ordersSumsExp
              )
             .groupBy(root.get("id"),
                     root.get("email"),
                     customerSnpExp,
                     root.get("phoneNumber"),
                     isRegisteredExp,
                     root.get("createdAt"));

        // Задать сортировку
        SortingUtils.createSortQueryForCustomers(cb,query, root, sortEnum, sortType,
                customerSnpExp, ordersCountExp, ordersUnitsCountExp, ordersSumsExp, avgUnitPriceExp);

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(limit);
        typedQuery.setFirstResult(limit*pageNum);

        List<Tuple> customers = typedQuery.getResultList();

        long elements = PaginationUtils.countCustomers(entityManager, filterDto);

        return new PageImpl<>(customers, PageRequest.of(pageNum, limit), elements);
    }

    @Override
    public CustomersFilterValuesDto getFilterValues() {

        Tuple tuple = customersRepository.getFilterValues();

        if (tuple == null)
            throw new ApiException("Получить пограничные значения для фильтрации покупателей не удалось!");

        return new CustomersFilterValuesDto(tuple);
    }

    @Override
    public void create(Customer customer) {
        if (customer == null || customer.getId() == null)
            throw new ApiException("Не удалось создать пользователя. Объект задан некорректно!");

        customersRepository.saveAndFlush(customer);
    }

    @Override
    public Customer create(CustomerRequestDto dto, String fingerprint, String ip) {
        // Добавить покупателя, если он не задан
        Customer existingCustomer = getCustomerByEmailOrId(dto.getEmail(), dto.getId());

        // Проверить наличие созданного посетителя с таким отпечатком браузера
        Visitor visitor = null;
        User user = Services.usersService.getByEmailNullable(dto.getEmail());

        if (fingerprint != null && !fingerprint.isEmpty()){
            visitor = Services.visitorsService.saveIfNotExists(fingerprint, ip);
        }

        if (existingCustomer == null)
            existingCustomer = customersRepository.save(new Customer(dto, visitor, user));
        else{
            // Получить запись о переданном покупателе и объекте из БД с тем же id
            Customer newCustomer = new Customer(dto, visitor, user);
            newCustomer.setId(existingCustomer.getId());
            newCustomer.setCreatedAt(existingCustomer.getCreatedAt());

            //Сравнить предыдущую запись с заданной
            if (!newCustomer.isEqualTo(existingCustomer)) {

                // Если покупатель изменён, но при этом в старой записи существует Visitor, а в новой нет
                if (newCustomer.getVisitor() == null && existingCustomer.getVisitor() != null)
                    newCustomer.setVisitor(existingCustomer.getVisitor());

                // Если покупатель изменён, но при этом в старой записи существует корректный User
                if (newCustomer.getUser() == null && existingCustomer.getUser() != null)
                    newCustomer.setUser(existingCustomer.getUser());

                //Если значения полей !=, тогда пересохранить запись, поскольку произошло редактирование
                existingCustomer = customersRepository.save(newCustomer);
            }

        }

        return existingCustomer;
    }

    @Override
    public Customer getCustomerByEmailOrId(String email, Long id) {

        if ((email == null || email.isBlank()) && id == null)
            throw new ApiException("Не удалось найти пользователя по email или id. Оба параметра заданы некорректно!");

        return email != null ? getCustomerByEmail(email) : customersRepository.findById(id).orElse(null);
    }

    @Override
    public void update(Customer customer) {
        if (customer == null || customer.getId() == null)
            throw new ApiException("Не удалось изменить пользователя. Объект задан некорректно!");

        customersRepository.saveAndFlush(customer);
    }

    @Override
    public Customer update(CustomerRequestDto dto) {

        if (dto == null || dto.getId() == null)
            throw new ApiException("Не удалось отредактировать запись покупателя. DTO задан некорректно!");

        Customer customer = customersRepository.findById(dto.getId())
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти покупателя с id: %d", dto.getId())));

        if (dto.getName() != null && !customer.getName().equals(dto.getName()))
            customer.setName(dto.getName());

        if (dto.getSurname() != null && !customer.getSurname().equals(dto.getSurname()))
            customer.setName(dto.getName());

        if (dto.getPatronymic() != null && !customer.getPatronymic().equals(dto.getPatronymic()))
            customer.setPatronymic(dto.getPatronymic());

        if (dto.getPhoneNumber() != null && customer.getPhoneNumber() != dto.getPhoneNumber())
            customer.setPhoneNumber(dto.getPhoneNumber());

        // Задать новый email
        if (dto.getEmail() != null && !customer.getEmail().equals(dto.getEmail()))
        {
            customer.setEmail(dto.getEmail());

            // Найти пользователя по новому email, если такой имеется
            User user = Services.usersService.getByEmailNullable(customer.getEmail());

            if (user != null)
                customer.setUser(user);
        }

        return customersRepository.saveAndFlush(customer);
    }

    @Override
    public Customer getById(Long id) {
        return customersRepository.findById(id).orElse(null);
    }

    @Override
    public Customer getCustomerByEmail(String email) {

        if (email == null || email.isBlank() || !Utils.emailIsValid(email))
            throw new ApiException("Не удалось найти пользователя по email. Задан некорректный параметр!");

        return customersRepository.getCustomerByEmail(email).orElse(null);
    }

}
