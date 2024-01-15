package gp.wagner.backend.repositories.categories;

import gp.wagner.backend.domain.entites.visits.CategoryViews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

//Репозиторий для просмотров категорий
@Repository
public interface CategoryViewsRepository extends JpaRepository<CategoryViews,Long> {

    //Добавление записи о просмотре категории
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into categories_views
            (visitor_id, category_id, count)
        values
            (:visitor, :category, :count)
    """)
    int insertCategoryView(@Param("visitor") long visitorId, @Param("category") long categoryId, @Param("count") int count);

    //Изменение кол-ва просмотров категории
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
    update categories_views set
                        visitor_id = :visitor,
                        category_id = :category,
                        count = :count
    where id = :id
    """)
    void updateCategoryView(@Param("id") long productViewId, @Param("visitor") long visitorId, @Param("category") long categoryId, @Param("count") int count);

    //Мягкое удаление

    //Получить maxId
    @Query(value = """
    select
        max(cv.id)
    from
        CategoryViews cv
    """)
    long getMaxId();

    //Выбрать просмотры категорий по id
    List<CategoryViews> findCategoryViewsByCategoryId(long categoryId);

    @Query(value = """
    select
        p
    from
        Product p
    where p.category.id = :category_id
    """)

    List<CategoryViews> findCategoryViewsByCategory(@Param("category_id")long categoryId);


    //Получить сумму просмотров всех дочерних категорий в родительской категории
    @Query(nativeQuery = true,
    value = """
    with recursive category_tree as (
                    -- Выборка просмотров родительской категории (базовый узел) НА КАЖДОМ УРОВНЕ РЕКУРСИИ
                    select
                        c.id as c_id,
                        c.category_name,
                        cv.count
                    from
                        categories_views cv join categories c on c.id = cv.category_id
                    where c.id = :category_id OR c.parent_id = :category_id
                    union
                    -- Выборка просмотров дочерних категорий
                    select
                        c.id as c_id,
                        c.category_name,
                        cv.count
                    -- Главная точка сравнения - parent_id здесь будет проверяться для каждого дочернего элемента
                    from categories_views cv join categories c on c.id = cv.category_id
                                             join category_tree ct on c.parent_id = ct.c_id
                    )
    select
        sum(ct.count)
    from category_tree ct;
    """)
    Integer countCategoryTreeViews(@Param("category_id")long parentCategoryId);
}
