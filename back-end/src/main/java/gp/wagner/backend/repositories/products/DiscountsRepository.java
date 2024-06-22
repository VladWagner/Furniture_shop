package gp.wagner.backend.repositories.products;

import gp.wagner.backend.domain.entities.products.Discount;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountsRepository extends JpaRepository<Discount,Long> {

    // Добавление скидки из DTO
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
    insert into discounts
       (percentage, starts_at, ends_at, is_active)
    values
        (:percentage, :starts_at, :ends_at,:is_active)
    """)
    void insert(@Param("percentage") float percentage, @Param("starts_at") Date startsAt, @Param("starts_at") Date endsAt,
                @Param("is_active") int isActive);


    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update discounts set percentage = :percentage,
                            starts_at = :starts_at,
                            ends_at = :starts_at,
                            is_active = :is_active
        where id = :discount_id
    """)
    void update(@Param("discount_id") long discountId, @Param("percentage") float percentage, @Param("starts_at") Date startsAt,
                @Param("starts_at") Date endsAt, @Param("is_active") boolean isActive);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update discounts set is_active = :is_active
        where id = :discount_id
    """)
    void updateIsActive(@Param("discount_id") long discountId, @Param("is_active") boolean isActive);

    // Задать скидку в варианты по списку id
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update variants_product pv set pv.discount_id = :discount_id
        where (pv.discount_id != :discount_id or pv.discount_id is null) and pv.id in :pv_ids_list
    """)
    void updatePvDiscountByIdsList(@Param("discount_id") long discountId, @Param("pv_ids_list") List<Long> pvIds);

    // Убрать заданную скидку у вариантов по списку id
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update variants_product pv set pv.discount_id = null
        where (pv.discount_id is not null and pv.discount_id = :discount_id) and pv.id in :pv_ids_list
    """)
    void removeDiscountFromPvByIdsList(@Param("discount_id") long discountId, @Param("pv_ids_list") List<Long> pvIds);

    // Задать скидку в варианты по списку id
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update variants_product pv set pv.discount_id = null
        where pv.discount_id is not null and pv.discount_id = :discount_id
    """)
    void deleteDiscountFromPV(@Param("discount_id") long discountId);

    @Query(value = """
        select
            pv
        from ProductVariant pv
        where (pv.discount is not null and pv.discount.id = :discount_id) and pv.product.id in :products_ids_list
    """)
    Optional<List<ProductVariant>> getVariantsForProductsWithDiscount(@Param("discount_id") long discountId, @Param("products_ids_list") List<Long> productsIds);

}
