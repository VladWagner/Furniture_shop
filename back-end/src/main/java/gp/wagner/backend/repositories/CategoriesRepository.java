package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.categories.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriesRepository extends JpaRepository<Category,Long> {

    //Добавление категории
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        insert into categories
        (category_name, parent_id, subcategory_id)
        values
        (:name, :parent_id, :repeating_category_id)
    """)
    void insertCategory(@Param("name") String categoryName,
                        @Param("parent_id") Integer parentCategoryId,
                        @Param("repeating_category_id") Integer repeatingCategoryId);

    //Добавление повторяющейся категории
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        insert into subcategories
         (sub_name)
        values
        (:name)
    """)
    void insertRepeatingCategory(@Param("name") String categoryName);


    //Изменение категории
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update categories set
                            category_name = :name,
                            parent_id = :parent_id,
                            subcategory_id = :repeating_category_id
        where id = :id
    """)
    void updateCategory(@Param("id") String categoryId, @Param("name") String categoryName,
                        @Param("parent_id") Integer parentCategoryId,
                        @Param("repeating_category_id") Integer repeatingCategoryId);

    //Получить родительские категории
    @Query(
    value = """
        select
            c.parentCategory
        from Category c
        where c = :id
    """)
    Optional<Category> getParentCategory(@Param("id") int categoryId);

    //Найти категорию по названию
    Optional<Category> findCategoryByName(String categoryName);

    //Получить максимальный id
    @Query(value = """
    select
        max(c.id)
    from
        Category c
    """)
    long getMaxId();


    //Получить абсолютно все дочерние категории - на всех уровнях рекурсии
    @Query(nativeQuery = true,
            value = """
        with recursive category_tree as (
            -- Выборка родительской категории (базовый узел) НА КАЖДОМ УРОВНЕ РЕКУРСИИ
            select
                id
            from
                categories
            where categories.id = :id
            union all
            -- Выборка дочерних категорий - рекурсивная часть
            select
                c.id
                -- Главная точка сравнения - parent_id здесь будет проверяться для каждого дочернего элемента
            from categories c join category_tree ct on c.parent_id = ct.id
        )
        
        select
            ct.id
        from category_tree ct;
    """)
    List<Long> getAllChildCategoriesIds(@Param("id") int parentCategoryId);

    //Получить абсолютно все дочерние категории - на одном уровне рекурсии
    @Query(nativeQuery = true,
            value = """
        select
            c.id
        from
            categories c
        where
            c.parent_id = :id
    """)
    List<Long> getChildCategoriesIds(@Param("id") int parentCategoryId);

}
