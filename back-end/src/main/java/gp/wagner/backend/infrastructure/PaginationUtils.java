package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.entites.visits.CategoryViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

// Класс для расчётов общего кол-ва элементов при пагинации выборки
public class PaginationUtils {

    // Подсчёт количества посетителей, которые просмотрели определённые категории
    public static Long countVisitorsWithProductsViews(EntityManager entityManager){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CategoryViews> root = query.from(CategoryViews.class);
        Path<Visitor> visitorPath = root.get("visitor");

        query.select(cb.countDistinct(visitorPath.get("id")));

        return entityManager.createQuery(query).getResultList().get(0);
    }

}
