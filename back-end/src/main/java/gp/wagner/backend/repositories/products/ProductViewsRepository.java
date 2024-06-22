package gp.wagner.backend.repositories.products;

import gp.wagner.backend.domain.entities.visits.ProductViews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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

    // Товары с кол-вом просмотров
    @Query(nativeQuery = true, value = """
            select
                p.id,
                SUM(ifnull(pviews.count,0)) as amount
            from products p left join products_views pviews on p.id = pviews.product_id
            group by p.id
            having
                amount >= 0
            """)
    List<Object[]> findProductsAndTheirViews(Pageable pageable);

    // Товары с максимальным кол-вом просмотров
    @Query(nativeQuery = true, value = """
                select
                    p.id,
                    SUM(ifnull(pviews.count,0)) as amount
                from products p left join products_views pviews on p.id = pviews.product_id
                group by p.id
                having
                     amount >= (
                     Select  MAX(sums)
                     from
                         (Select SUM(pviews.count) as sums
                          from products p left join products_views pviews on p.id = pviews.product_id
                          group by p.id) as views_sums);
            """)
    Page<Object[]> findProductsWithMaxViews(Pageable pageable);

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
