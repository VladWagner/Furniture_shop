package gp.wagner.backend.repositories.admin_panel;

import gp.wagner.backend.domain.entities.visits.Visitor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AdminPanelStatisticsRepository extends JpaRepository<Visitor,Long> {

    // Подсчёт кол-ва посещений по дням за определённый период
    @Query(nativeQuery = true, value = """
            select
               dv.date,
               sum(dv.count) as visits_amount
            from
                daily_visits dv
            where
                dv.date between :date_lo and :date_hi
            group by dv.date
            """)
    Page<Object[]> getDailyVisitsBetweenDates(@Param("date_lo") Date dateLo,@Param("date_hi") Date dateHi, Pageable pageable);

    // Общая сумма посещений за определённый период
    @Query(nativeQuery = true, value = """
            with visits_in_dates as (select
               dv.date,
               sum(dv.count) as visits_amount
            from
                daily_visits dv
            where
                dv.date between :date_lo and :date_hi
            group by dv.date
            )
            
            select
                coalesce(sum(vid.visits_amount), 0) as visits_sum
            from visits_in_dates vid;
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
            with orders_count as (select
                DATE(o.order_date) as order_date_alias,
                COUNT(o.id) as orders_count,
                SUM(o.sum) as orders_sum
            from
                orders o
            where
                o.order_date between :date_lo and :date_hi and
                ((:state_id is not null and :state_id > 0 and o.order_state_id = :state_id)
                      or :state_id is null or :state_id <= 0)
            group by order_date_alias)
        
            select
            *
            from orders_count oc
            """)
    Page<Object[]> getOrdersByDaysBetweenDates(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("state_id") Integer orderStateId, Pageable pageable);

    // Конверсия из просмотра в заказ в определённой категории и диапазоне дат – стоит переделать в Criteria API, потому что если сюда добавить
    // ещё и выборку по статусу
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select sum(dv.count) from daily_visits dv where dv.date = order_date_alias), 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount,
        SUM(o.sum) as orders_sum
    from orders o left join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        ((:category_id is not null and :category_id > 0 and p.category_id = :category_id)
            or :category_id is null or :category_id <= 0 ) and
        ((:state_id > 0 and o.order_state_id = :state_id) or :state_id <= 0)
    group by order_date_alias, visits),
    
    doc_with_cvr as (
     select
         *,
         doc.order_date_alias as order_date,
         coalesce(doc.orders_amount/visits, 0) as cvr
     from
         date_and_orders_count doc
     where
         doc.visits >= doc.orders_amount)
  
     select
         dwc.order_date,
         dwc.orders_amount,
         dwc.visits,
         dwc.cvr,
         dwc.orders_sum
     from
         doc_with_cvr dwc
    
""")
    Page<Object[]> getCvrToOrdersBetweenDatesInCategory(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("category_id") Long categoryId,
                                                        @Param("state_id") int orderStateId, Pageable pageable);
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select sum(dv.count) from daily_visits dv where dv.date = order_date_alias), 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount,
        SUM(o.sum) as orders_sum
    from orders o left join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        p.category_id in :category_ids and
        ((:state_id > 0 and o.order_state_id = :state_id) or :state_id <= 0)
    group by order_date_alias, visits),
    
       doc_with_cvr as (
        select
            *,
            doc.order_date_alias as order_date,
            coalesce(doc.orders_amount/visits, 0) as cvr
        from
            date_and_orders_count doc
        where
            doc.visits >= doc.orders_amount)
  
        select
            dwc.order_date,
            dwc.orders_amount,
            dwc.visits,
            dwc.cvr,
            dwc.orders_sum
        from
            doc_with_cvr dwc
    
""")
    Page<Object[]> getCvrToOrdersBetweenDatesInCategoriesIds(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("category_ids") List<Long> categoryIds,
                                                             @Param("state_id") int orderStateId,  Pageable pageable);

    // Конверсия из просмотра в заказ определённого товара в диапазоне дат – так же стоит переделать в Criteria API
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select sum(dv.count) from daily_visits dv where dv.date = order_date_alias)/*(select count(v.id)
            from visitors v where v.last_visit_at = order_date_alias group by v.last_visit_at)*/, 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount,
        SUM(o.sum) as orders_sum
    from orders o left join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        ((:product_id is not null and :product_id > 0 and p.id = :product_id)
                or :product_id is null or :product_id <= 0) and
        ((:state_id > 0 and o.order_state_id = :state_id) or :state_id <= 0)
    group by order_date_alias, visits),
    
   doc_with_cvr as (
    select
        *,
        doc.order_date_alias as order_date,
        coalesce(doc.orders_amount/visits, 0) as cvr
    from
        date_and_orders_count doc
    where
        doc.visits >= doc.orders_amount)

    select
        dwc.order_date,
        dwc.orders_amount,
        dwc.visits,
        dwc.cvr,
        dwc.orders_sum
    from
        doc_with_cvr dwc
""")
    Page<Object[]> getCvrToOrdersBetweenDatesForProduct(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("product_id") Long productId,
                                                        @Param("state_id") int orderStateId,  Pageable pageable);

    // Конверсия из просмотра в добавление в корзину для конкретного товара
    @Query(nativeQuery = true, value = """
    with baskets_count as (select
        bdc.add_date_alias as add_date,
        bdc.addings_amount,
        bdc.visits,
        coalesce(bdc.addings_amount/visits, 0) as cvr,
        bdc.baskets_sum
    from (select
              DATE(b.added_date) as add_date_alias,
              coalesce((select sum(dv.count) from daily_visits dv where dv.date = add_date_alias), 0) as visits,
              COUNT(DISTINCT(b.id)) as addings_amount,
              SUM(b.sum) as baskets_sum
          from baskets b left join (baskets_products_variants bpv join
              (variants_product vp join products p on vp.product_id = p.id)
                              on bpv.product_variant_id = vp.id)
                             on b.id = bpv.basket_id
          where b.added_date between :date_lo and :date_hi and
        ((:product_id is not null and :product_id > 0 and p.id = :product_id)
                or :product_id is null or :product_id <= 0)
          group by add_date_alias, visits) as bdc)
    select
        *
    from baskets_count
""")
    Page<Object[]> getCvrToBasketsBetweenDatesForProduct(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("product_id") Long productId,
                                                        Pageable pageable);

    // Максимальная, средняя и минимальная конверсия за период в категории
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select sum(dv.count) from daily_visits dv where dv.date = order_date_alias), 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount
    from orders o join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        ((:category_id is not null and :category_id > 0 and p.category_id = :category_id)
            or :category_id is null or :category_id <= 0 ) and
        ((:state_id > 0 and o.order_state_id = :state_id) or :state_id <= 0)
    group by order_date_alias, visits)
    
    select
        coalesce(min(doc.orders_amount/visits),0) as min_cvr,
        coalesce(avg(doc.orders_amount/visits),0) as avg_cvr,
        coalesce(max(doc.orders_amount/visits),0) as max_cvr
    from date_and_orders_count doc
    where doc.visits >= doc.orders_amount;
""")
    Object[][] getQuantityValuesForOrdersBetweenDatesInCategory(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("category_id") Long categoryId,
                                                                @Param("state_id") int orderStateId);

    // Максимальная, средняя и минимальная конверсия за период для списка категорий
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select sum(dv.count) from daily_visits dv where dv.date = order_date_alias), 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount
    from orders o join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        p.category_id in :category_ids_list and
        ((:state_id > 0 and o.order_state_id = :state_id) or :state_id <= 0)
    group by order_date_alias, visits)
    
    select
        coalesce(min(doc.orders_amount/visits),0) as min_cvr,
        coalesce(avg(doc.orders_amount/visits),0) as avg_cvr,
        coalesce(max(doc.orders_amount/visits),0) as max_cvr
    from date_and_orders_count doc
    where doc.visits >= doc.orders_amount;
""")
    Object[][] getQuantityValuesForOrdersBetweenDatesInCategories(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("category_ids_list") List<Long> categoryIds,
                                                                @Param("state_id") int orderStateId);

    // Максимальная, средняя и минимальная конверсия за период для товара
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (select
        DATE(o.order_date) as order_date_alias,
        coalesce((select sum(dv.count) from daily_visits dv where dv.date = order_date_alias), 0) as visits,
        COUNT(DISTINCT(o.id)) as orders_amount
    from orders o left join (orders_products_variants opv join
                                (variants_product vp join products p on vp.product_id = p.id)
                                on opv.product_variant_id = vp.id)
            on o.id = opv.order_id
    where o.order_date between :date_lo and :date_hi and
        ((:product_id is not null and :product_id > 0 and p.id = :product_id)
                or :product_id is null or :product_id <= 0) and
        ((:state_id > 0 and o.order_state_id = :state_id) or :state_id <= 0)
    group by order_date_alias, visits)
    
    select
        coalesce(min(doc.orders_amount/visits),0) as min_cvr,
        coalesce(avg(doc.orders_amount/visits),0) as avg_cvr,
        coalesce(max(doc.orders_amount/visits),0) as max_cvr
    from date_and_orders_count doc
    where doc.visits >= doc.orders_amount;
""")
    Object[][] getQuantityValuesForOrdersBetweenDatesForProduct(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("product_id") Long productId,
                                                                @Param("state_id") int orderStateId);

    // Частота просмотров товаров в разных категориях - обрати внимание на countQuery, без него такое количество подзапросов не работает
    @Query(nativeQuery = true, value = """
     with products_views_frequency as (select
        c.id as category_id,
        coalesce(c.category_name, sub_c.sub_name) as categ_name,
        p_categ.id as patent_id,
        (select coalesce(sum(pviews.count),0) from products_views pviews join products p on p.id = pviews.product_id
            where p.category_id = c.id) as views_amount,
        (select count(pviews.visitor_id) from products_views pviews join products p on p.id = pviews.product_id
            where p.category_id = c.id) as visitors_amount,
        (select coalesce(sum(pviews.count)/count(pviews.visitor_id),0) from products_views pviews join products p on p.id = pviews.product_id
            where p.category_id = c.id) as frequency
    
    from categories c left join subcategories sub_c on c.subcategory_id = sub_c.id
                      join categories as p_categ on c.parent_id = p_categ.id)
    select
        *
    from products_views_frequency
""", countQuery = """
    select
        count(*)
    from categories c left join subcategories sub_c on c.subcategory_id = sub_c.id
                      join categories as p_categ on c.parent_id = p_categ.id
""")
    Page<Object[]> getProdViewsFrequencyInCategories(Pageable pageable);

    // Частота просмотров категорий относительно уникальных пользователей
    @Query(nativeQuery = true, value = """
    with products_views_frequency as (select
        c.id as category_id,
        coalesce(c.category_name, sub_c.sub_name) as categ_name,
        p_categ.id as patent_id,
        (select coalesce(sum(cviews.count),0) from categories_views cviews where cviews.category_id = c.id) as views_amount,
        (select count(cviews.visitor_id) from categories_views cviews where cviews.category_id = c.id) as visitors_amount,
        (select coalesce(sum(cviews.count)/count(cviews.visitor_id),0) from categories_views cviews where cviews.category_id = c.id) as frequency
    
    from categories c left join subcategories sub_c on c.subcategory_id = sub_c.id
                      join categories as p_categ on c.parent_id = p_categ.id)
    select
        *
    from products_views_frequency pvf
""", countQuery = """
    select
        count(*)
    from categories c left join subcategories sub_c on c.subcategory_id = sub_c.id
                      join categories as p_categ on c.parent_id = p_categ.id
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
