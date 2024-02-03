package gp.wagner.backend.domain.specifications;

import gp.wagner.backend.domain.dto.request.filters.UsersFilterRequestDto;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.users.UserRole;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

//Спецификации для формирования hibernate'ом условий выборок из ДБ
public class UsersSpecifications {


    // Создать спецификацию выборки пользователей
    public static Specification<User> createGeneralUsersFilterSpecification(UsersFilterRequestDto filterDto) {

        if (filterDto == null)
            return null;

        return  (root, query, cb) -> {

            // Если задан конкретный id, тогда дальше ничего не выбираем
            if (filterDto.getId() != null)
                return cb.equal(root.get("id"), filterDto.getId());


            List<Predicate> predicates = new ArrayList<>();

            if (filterDto.getRole() != null && filterDto.getRole() > 0) {
                Path<UserRole> rolePath = root.get("userRole");
                predicates.add(cb.equal(rolePath.get("id"), filterDto.getRole()));
            }

            // Флаг подтверждения почты
            if (filterDto.getIsConfirmed() != null)
                predicates.add(cb.equal(root.get("isConfirmed"), filterDto.getIsConfirmed()));

            // Начальная дата регистрации
            if (filterDto.getMinDate() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filterDto.getMinDate()));

            // Конечная дата регистрации
            if (filterDto.getMaxDate() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filterDto.getMaxDate()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };// and
    }
}

