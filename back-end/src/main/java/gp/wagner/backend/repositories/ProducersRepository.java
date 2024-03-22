package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Producer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProducersRepository extends JpaRepository<Producer,Long> {

    //Добавление категории
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        insert into producers
        (producer_name, producer_img)
        values
        (:name, :logo)
    """)
    void insertProducer(@Param("name") String producerName, @Param("logo") String producerLogo);

    // Обновить товары производителя при его удалении
    @Transactional
    @Query(nativeQuery = true, value = """
    update products
        set
            is_deleted = true
    where producer_id = :producer_id
    
""")
    void deleteProducerProducts(@Param("producer_id") long producerId);

    //Изменение производителя
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update producers set
                            producer_name = :name,
                            producer_img = :logo
        where id = :id
    """)
    void updateProducer(@Param("id") int productId, @Param("name") String producerName, @Param("logo") String producerLogo);

    //Получить максимальный id
    @Query(value = """
    select
        max(p.id)
    from
        Producer p
    """)
    long getMaxId();

    Optional<Producer> getProducerById(long producerId);

    //Получить производителей в определённой категории
    @Query(value = """
    select
        p.producer
    from
        Product p
    where
        p.category.id = :category_id
    
""")
    List<Producer> getProducersInCategory(@Param("category_id") long categoryId);

    //Получить производителей в списке категорий
    @Query(value = """
    select
        p.producer
    from
        Product p
    where
        p.category.id in :category_id_list
    
""")
    List<Producer> getProducersInCategories(@Param("category_id_list") List<Long> categoriesIds);

    //Получить производителей по ключевому слову в товарах
    @Query(value = """
    select
        p.producer
    from
        Product p join ProductVariant pv on pv.product = p
    where
        p.name like concat('%',:keyword,'%') or
        p.description like concat('%',:keyword,'%') or
        pv.title like concat('%',:keyword,'%') or
        p.producer.producerName like concat('%',:keyword,'%')
    
""")
    List<Producer> getProducersByProductKeyword(@Param("keyword") String key);

    @Query(value = """
    select
        p
    from Producer p
    where p.deletedAt is not null
""")
    Page<Producer> getDeletedProducers(PageRequest of);
}
