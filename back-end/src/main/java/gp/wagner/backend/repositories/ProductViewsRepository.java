package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import jakarta.annotation.Nullable;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface ProductViewsRepository extends JpaRepository<ProductViews,Long> {

    //Добавление записи о просмотре товара
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into products_views
        (visitor_id, product_id, count) 
        values 
        (:visitor, :product, :count)
    """)
    int insertProductView(@Param("visitor") long visitorId, @Param("product") long productId, @Param("count") int count);

    //Изменение посетителя
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
    update products_views set
                        visitor_id = :visitor,
                        product_id = :product,
                        count = :count
    where id = :viewId
    """)
    void updateProductView(@Param("viewId") long productViewId,@Param("visitor") long visitorId, @Param("product") long productId, @Param("count") int count);

    //Мягкое удаление

    //Получить maxId
    @Query(value = """
    select
        max(pv.id)
    from
        ProductViews pv
    """)
    long getMaxId();
}
