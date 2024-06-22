package gp.wagner.backend.repositories.baskets;

import gp.wagner.backend.domain.entities.baskets.Basket;
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
            (user_id)
        values
        (user_id)
    """)
    void insertBasket(@Param("user_id") int userId);

    //Изменение посетителя
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update baskets set
              user_id = :user_id,
              added_date = :date
   where id = :id
    """)
    void updateBasket(@Param("id") long id, @Param("user_id") int userId,
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
