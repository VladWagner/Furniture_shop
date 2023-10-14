package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.products.Product;
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

}
