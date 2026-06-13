package com.example.smartPurchase.product.repository

import com.example.smartPurchase.product.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface ProductRepository : JpaRepository<Product, Long> {

    @Query(
        value = """
        SELECT
            p.id AS id,
            p.name AS name,
            p.category AS category,
            p.price AS price,
            STRING_AGG(DISTINCT wi.size, ',') AS availableSizes
        FROM products p
        JOIN warehouse_inventory wi
            ON p.id = wi.product_id
        JOIN warehouses w
            ON wi.warehouse_id = w.id
        WHERE
            (:category IS NULL OR p.category = :category)
        AND (:size IS NULL OR wi.size = :size)
        AND (:city IS NULL OR w.city = :city)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND wi.quantity > 0
        GROUP BY
            p.id,
            p.name,
            p.category,
            p.price
    """,
        nativeQuery = true
    )
    fun search(
        @Param("category") category: String?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
        @Param("size") size: String?,
        @Param("city") city: String?
    ): List<ProductSearchProjection>

    @Query(
        value = """
            SELECT
                p.id AS id,
                p.name AS name,
                p.category AS category,
                p.price AS price,
                wi.size AS size,
                wi.quantity AS quantity
            FROM products p
            LEFT JOIN warehouse_inventory wi
                ON p.id = wi.product_id
            WHERE p.id = :productId
        """,
        nativeQuery = true
    )
    fun findProductDetails(
        @Param("productId") productId: Long
    ): List<ProductDetailsProjection>
}