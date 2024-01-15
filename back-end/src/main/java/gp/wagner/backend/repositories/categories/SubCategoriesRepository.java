package gp.wagner.backend.repositories.categories;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.categories.RepeatingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SubCategoriesRepository extends JpaRepository<RepeatingCategory,Long> {


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
        update subcategories set
                            sub_name = :name,
        where id = :id
    """)
    void updateCategory(@Param("id") String categoryId, @Param("name") String categoryName);


    //Найти категорию по названию
    Optional<RepeatingCategory> findRepeatingCategoryByName(String categoryName);

    //Получить максимальный id
    @Query(value = """
    select
        max(rc.id)
    from
        RepeatingCategory rc
    """)
    long getMaxId();
}
