package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.basket.Basket;
import gp.wagner.backend.domain.entites.visits.Visitor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Repository
public interface BasketsRepository extends JpaRepository<Basket,Long> {

    //Добавление записи о посетителе
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into baskets
            (product_variant_id, user_id, products_amount, added_date)
        values
        (:pv_id, :user_id, :products_amount, :date)
    """)
    void insertBasket(@Param("pv_id") long productVariantId, @Param("user_id") int userId, @Param("products_amount") int products_count,
                      @Param("date") Date addingDate);

    //Изменение посетителя
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update baskets set
              product_variant_id = :pv_id,
              user_id = :user_id,
              products_amount = :products_amount,
              added_date = :date
   where id = :id
    """)
    void updateBasket(@Param("id") long id, @Param("pv_id") long productVariantId, @Param("user_id") int userId, @Param("products_amount") int products_count,
                      @Param("date") Date addingDate);


    //Получить maxId
    @Query(value = """
    select
        max(b.id)
    from
        Basket b
    """)
    long getMaxId();


}
