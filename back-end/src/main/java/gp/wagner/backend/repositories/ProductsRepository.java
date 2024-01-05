package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.infrastructure.SimpleTuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {

    //Добавление товара из DTO
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
    insert into products
       (product_name, description, category_id, producer_id, is_available, show_product)
    values 
        (:productName,:description,:categoryId,:producerId,:isAvailable,:showProduct)
    """)
    void insertProduct(@Param("productName") String productName, @Param("description") String description,
                       @Param("categoryId") int categoryId, @Param("producerId") int producerId,
                       @Param("isAvailable") int isAvailable, @Param("showProduct") int showProduct);

    //Изменение товара из DTO
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        update products set product_name = :productName,
                            products.description = :description,
                            category_id = :categoryId,
                            producer_id = :producerId,
                            is_available = :isAvailable,
                            show_product = :showProduct
        where id = :productId
    """)
    void updateProduct(@Param("productId") long productId,@Param("productName") String productName, @Param("description") String description,
                       @Param("categoryId") long categoryId, @Param("producerId") long producerId,
                       @Param("isAvailable") int isAvailable, @Param("showProduct") int showProduct);

    //Выборка с фильтрацией товаров по характеристикам

    /*
    (attrVal.attribute.id = 1 and attrVal.intValue between 700 and 2200) and
    (select min(prodVariant.id) from ProductVariant prodVariant where prodVariant.product = p) = pv.id and
    pv.price between 15000 and 90000*/

    @Query(value = """

    select
        p
    from
        Product as p join Category category on p.category = category
                     join AttributeValue attrVal on attrVal.product = p
                     join ProductVariant pv on pv.product = p
    where (attrVal.attribute.id = 1 and attrVal.intValue between 700 and 2200) and
              (select min(prodVariant.id) from ProductVariant prodVariant where prodVariant.product = p) = pv.id and
              pv.price between 15000 and 90000
    """)
    List<Product> findProductsByFilter(@Param("whereClause") String whereClaue);

    //Получение максимального id
    @Query(value = """
    select
        max(p.id)
    from
        Product p
    """)
    long getMaxId();

    //Найти все товары в определённой категории
    Page<Product> findProductsByCategoryId(Long category_id, Pageable pageable);

    //Найти диапазон цен для товаров в определённой категории
    @Query(nativeQuery = true, value = """
    select
        MIN(vp.price) as min_set,
        MAX(vp.price) as max_set
    from
        products p join variants_product vp on  p.id = vp.product_id
    where
        (:category_id > 0 and p.category_id = :category_id) or :category_id <= 0
    """)
    Object getMinMaxPriceInCategory(@Param("category_id") long categoryId);

    //Найти диапазон цен для товаров в нескольких категориях
    @Query(nativeQuery = true, value = """
    select
        MIN(vp.price) as min_set,
        MAX(vp.price) as max_set
    from
        products p join variants_product vp on  p.id = vp.product_id
    where
        p.category_id in :category_ids_list
    """)
    Object getMinMaxPriceInCategories(@Param("category_ids_list") List<Long> categoriesIds);

    //Найти диапазон цен для товаров в товарах с заданным ключевым словом
    @Query(nativeQuery = true, value = """
    select
        MIN(pv.price) as min_set,
        MAX(pv.price) as max_set
    from
        products p join variants_product pv on p.id = pv.product_id
                   join producers producer on p.producer_id = producer.id
    where
        p.product_name like concat('%',:keyword,'%') or
        p.description like concat('%',:keyword,'%') or
        pv.title like concat('%',:keyword,'%') or
        producer.producer_name like concat('%',:keyword,'%')
    """)
    Object getMinMaxPriceByKeyword(@Param("keyword") String keyword);

    // Базовый полнотекстовый поиск товаров - примитивный через like без транслитераций и неправильной раскладки
    @Query(value = """
    select distinct
    p
    from
        Product p join ProductVariant pv on pv.product = p
    where
        p.name like concat('%',:keyword,'%') or
        p.description like concat('%',:keyword,'%') or
        pv.title like concat('%',:keyword,'%') or
        p.producer.producerName like concat('%',:keyword,'%')
""")
    Page<Product> findProductsByKeyword(@Param("keyword") String key, Pageable pageable);


}
