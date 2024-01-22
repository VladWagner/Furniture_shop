package gp.wagner.backend.repositories.admin_panel;

import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface AdminPanelStatisticsRepository extends JpaRepository<Visitor,Long> {

    // Подсчёт кол-ва посещений по дням за определённый период
    @Query(nativeQuery = true, value = """
            select
                v.last_visit_at,
                COUNT(*) as visits_amount
            from
                visitors v
            where
                v.last_visit_at between :date_lo and :date_hi
            group by v.last_visit_at
            """)
    Page<Object[]> getDailyVisitsBetweenDates(@Param("date_lo") Date dateLo,@Param("date_hi") Date dateHi, Pageable pageable);

    // Общая сумма посещений за определённый период
    @Query(nativeQuery = true, value = """
            with visistors_in_dates as (select
                v.last_visit_at,
                COUNT(*) as visits_amount
            from
                visitors v
            where v.last_visit_at between :date_lo and :date_hi
            group by v.last_visit_at
            )
            
            select
                coalesce(sum(vid.visits_amount), 0) as visits_sum
            from visistors_in_dates vid;
            """)
    long getSumDailyVisitsBetweenDates(@Param("date_lo") Date dateLo,@Param("date_hi") Date dateHi);


    // Подсчёт кол-ва посещений по дням за определённый период из таблицы daily_visits
    @Query(nativeQuery = true, value = """
            select
                dv.date,
                dv.count
            from
                daily_visits dv
            where dv.date between :date_lo and :date_hi
            """)
    Page<Object[]> getDailyVisitsBetweenDatesInBV(@Param("date_lo") Date dateLo,@Param("date_hi") Date dateHi, Pageable pageable);

    // Подсчёт суммы посещений за определённый период из таблицы daily_visits
    @Query(nativeQuery = true, value = """
            select
                coalesce(SUM(dv.count), 0) as sum
            from
                daily_visits dv
            where
                dv.date between :date_lo and :date_hi
            """)
    long getSumDailyVisitsBetweenDatesInBV(@Param("date_lo") Date dateLo,@Param("date_hi") Date dateHi);

    // Подсчёт кол-ва заказов по дням за определённый период
    @Query(nativeQuery = true, value = """
            select
                DATE(o.order_date),
                COUNT(o.id),
                SUM(o.sum)
            from
                orders o
            where
                o.order_date between :date_lo and :date_hi and
                ((:state is not null and :state > 0 and o.order_state_id = :state)
                      or :state is null or :state <= 0)
            group by o.order_date
            """)
    Page<Object[]> getOrdersByDaysBetweenDates(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("state") Long orderStateId, Pageable pageable);

    // Конверсия из просмотра в заказ в определённой категории и диапазоне дат – стоит переделать в Criteria API, потому что если сюда добавить
    // ещё и выборку по статусу
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select count(v.id)
            from visitors v where v.last_visit_at = order_date_alias group by v.last_visit_at), 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount
    from orders o left join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        ((:category_id is not null and :category_id > 0 and p.category_id = :category_id)
            or :category_id is null or :category_id <= 0 )
    group by order_date_alias, visits)
    
    select
        doc.order_date_alias,
        doc.orders_amount,
        doc.visits,
        coalesce(doc.orders_amount/visits, 0) as cvr
    from date_and_orders_count doc
    where doc.visits >= doc.orders_amount;
""")
    Page<Object[]> getCvrToOrdersBetweenDatesInCategory(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("category_id") Long categoryId,
                                               Pageable pageable);

    // Конверсия из просмотра в заказ определённого товара в диапазоне дат – так же стоит переделать в Criteria API
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select count(v.id)
            from visitors v where v.last_visit_at = order_date_alias group by v.last_visit_at), 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount
    from orders o left join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        ((:product_id is not null and :product_id > 0 and p.id = :product_id)
                or :product_id is null or :product_id <= 0)
    group by order_date_alias, visits)
    
    select
        doc.order_date_alias,
        doc.orders_amount,
        doc.visits,
        coalesce(doc.orders_amount/visits, 0) as cvr
    from date_and_orders_count doc
    where doc.visits >= doc.orders_amount;
""")
    Page<Object[]> getCvrToOrdersBetweenDatesForProduct(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("product_id") Long productId,
                                               Pageable pageable);

    // Конверсия из просмотра в добавление в корзину для конкретного товара
    @Query(nativeQuery = true, value = """
    select
        bdc.add_date_alias,
        bdc.addings_amount,
        bdc.visits,
        coalesce(bdc.addings_amount/visits*100, 0) as cvr
    from (select
              DATE(b.added_date) as add_date_alias,
              coalesce((select count(v.id)
                        from visitors v where v.last_visit_at = add_date_alias group by v.last_visit_at), 0) as visits,
              COUNT(DISTINCT(b.id)) as addings_amount
          from baskets b left join (baskets_products_variants bpv join
              (variants_product vp join products p on vp.product_id = p.id)
                              on bpv.product_variant_id = vp.id)
                             on b.id = bpv.basket_id
          where b.added_date between :date_lo and :date_hi and
        ((:product_id is not null and :product_id > 0 and p.id = :product_id)
                or :product_id is null or :product_id <= 0)
          group by add_date_alias, visits) as bdc;
""")
    Page<Object[]> getCvrToBasketsBetweenDatesForProduct(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("product_id") Long productId,
                                                        Pageable pageable);

    // Максимальная, средняя и минимальная конверсия за период в категории
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select count(v.id)
            from visitors v where v.last_visit_at = order_date_alias group by v.last_visit_at), 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount
    from orders o join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        ((:category_id is not null and :category_id > 0 and p.category_id = :category_id)
            or :category_id is null or :category_id <= 0 )
    group by order_date_alias, visits)
    
    select
        coalesce(min(doc.orders_amount/visits),0) as min_cvr,
        coalesce(avg(doc.orders_amount/visits),0) as avg_cvr,
        coalesce(max(doc.orders_amount/visits),0) as max_cvr
    from date_and_orders_count doc
    where doc.visits >= doc.orders_amount;
""")
    Object[][] getQuantityValuesForOrdersBetweenDatesInCategory(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("category_id") Long categoryId);

    // Максимальная, средняя и минимальная конверсия за период для товара
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select count(v.id)
            from visitors v where v.last_visit_at = order_date_alias group by v.last_visit_at), 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount
    from orders o left join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        ((:product_id is not null and :product_id > 0 and p.id = :product_id)
                or :product_id is null or :product_id <= 0)
    group by order_date_alias, visits)
    
    select
        coalesce(min(doc.orders_amount/visits),0) as min_cvr,
        coalesce(avg(doc.orders_amount/visits),0) as avg_cvr,
        coalesce(max(doc.orders_amount/visits),0) as max_cvr
    from date_and_orders_count doc
    where doc.visits >= doc.orders_amount;
""")
    Object[][] getQuantityValuesForOrdersBetweenDatesForProduct(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("product_id") Long productId);

    // Частота просмотров товаров в разных категориях
    @Query(nativeQuery = true, value = """
    select
        c.id as category_id,
        coalesce(c.category_name, sub_c.sub_name) as categ_name,
        (select coalesce(sum(pviews.count),0) from products_views pviews join products p on p.id = pviews.product_id
                  where p.category_id = c.id) as products_views,
        (select count(pviews.visitor_id) from products_views pviews join products p on p.id = pviews.product_id
                  where p.category_id = c.id) as visitors_amount,
        (select coalesce(sum(pviews.count)/count(pviews.visitor_id),0) from products_views pviews join products p on p.id = pviews.product_id
                  where p.category_id = c.id) as frequecny
    
    from categories c left join subcategories sub_c on c.subcategory_id = sub_c.id
                      join categories as p_categ on c.parent_id = p_categ.id
    order by frequecny desc
""")
    Page<Object[]> getProwViewsFrequencyInCategories(Pageable pageable);

    // Частота просмотров категорий относительно уникальных пользователей
    @Query(nativeQuery = true, value = """
    select
        c.id as category_id,
        coalesce(c.category_name, sub_c.sub_name) as categ_name,
        (select coalesce(sum(cviews.count),0) from categories_views cviews where cviews.category_id = c.id) as products_views,
        (select count(cviews.visitor_id) from categories_views cviews where cviews.category_id = c.id) as visitors_amount,
        (select coalesce(sum(cviews.count)/count(cviews.visitor_id),0) from categories_views cviews where cviews.category_id = c.id) as frequecny
    
    from categories c left join subcategories sub_c on c.subcategory_id = sub_c.id
                      join categories as p_categ on c.parent_id = p_categ.id
    order by frequecny desc
""")
    Page<Object[]> getCategoriesViewsFrequency(Pageable pageable);


    //Получить maxId
    @Query(value = """
    select
        max(pv.id)
    from
        ProductViews pv
    """)
    long getMaxId();
}
