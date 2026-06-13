package com.example.smartPurchase.product.repository

import com.example.smartPurchase.product.entity.Product
import com.example.smartPurchase.product.entity.ProductSize
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface ProductRepository : JpaRepository<Product, Long> {

    @Query(
        """
        SELECT p
        FROM Product p
        WHERE
            (:category IS NULL OR p.category = :category)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND EXISTS (
            SELECT 1
            FROM WarehouseInventory wi
            JOIN wi.warehouse w
            WHERE wi.product = p
            AND wi.quantity > 0
            AND (:size IS NULL OR wi.size = :size)
            AND (:city IS NULL OR w.city = :city)
        )
        """
    )
    fun findProducts(
        @Param("category") category: String?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
        @Param("size") size: ProductSize?,
        @Param("city") city: String?,
        pageable: Pageable
    ): Page<Product>

    @Query(
        """
            SELECT DISTINCT
                wi.product.id AS productId,
                wi.size AS size
            FROM WarehouseInventory wi
            WHERE wi.product.id IN (:productIds)
            AND wi.quantity > 0
            ORDER BY wi.product.id, wi.size
        """
    )
    fun findAvailableSizesByProductIds(
        @Param("productIds") productIds: Collection<Long>
    ): List<ProductSizeProjection>

    @Query(
        """
            SELECT p
            FROM Product base, Product p
            WHERE base.id = :productId
            AND p.category = base.category
            AND p.id <> base.id
            AND p.price BETWEEN base.price - :priceRange AND base.price + :priceRange
            AND EXISTS (
                SELECT 1
                FROM WarehouseInventory wi
                WHERE wi.product = p
                AND wi.quantity > 0
            )
            ORDER BY ABS(p.price - base.price), p.id
        """
    )
    fun findSimilarProducts(
        @Param("productId") productId: Long,
        @Param("priceRange") priceRange: BigDecimal,
        pageable: Pageable
    ): List<Product>

    @Query(
        """
            SELECT
                p.id AS id,
                p.name AS name,
                p.category AS category,
                p.price AS price,
                p.imageUrl AS imageUrl,
                wi.size AS size,
                wi.quantity AS quantity
            FROM Product p
            LEFT JOIN WarehouseInventory wi
                ON wi.product = p
            WHERE p.id = :productId
        """
    )
    fun findProductDetails(
        @Param("productId") productId: Long
    ): List<ProductDetailsProjection>
}