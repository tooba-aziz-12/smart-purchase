package com.example.smartPurchase.product.service

import com.example.smartPurchase.common.dto.PageResponse
import com.example.smartPurchase.product.dto.PriceBreakdownResponse
import com.example.smartPurchase.product.dto.ProductDetailsResponse
import com.example.smartPurchase.product.dto.ProductResponse
import com.example.smartPurchase.product.dto.SizeOptionResponse
import com.example.smartPurchase.product.config.PricingProperties
import com.example.smartPurchase.product.entity.Product
import com.example.smartPurchase.product.entity.ProductSize
import com.example.smartPurchase.product.exception.InvalidProductFilterException
import com.example.smartPurchase.product.exception.ProductNotFoundException
import com.example.smartPurchase.product.repository.ProductDetailsProjection
import com.example.smartPurchase.product.repository.ProductRepository
import com.example.smartPurchase.util.DeliveryEstimator
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val deliveryEstimator: DeliveryEstimator,
    private val pricingProperties: PricingProperties

) {

    fun getProducts(
        category: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        size: String?,
        deliverTo: String?,
        page: Int,
        pageSize: Int
    ): PageResponse<ProductResponse> {
        validateFilters(
            minPrice,
            maxPrice,
            size,
            page,
            pageSize
        )

        val parsedSize = size?.let(ProductSize::valueOf)

        val productPage = productRepository.findProducts(
            category,
            minPrice,
            maxPrice,
            parsedSize,
            PageRequest.of(
                page,
                pageSize,
                Sort.by("id").ascending()
            )
        )

        val productIds = productPage.content.map { it.id }

        val availableSizes = if (productIds.isEmpty()) {
            emptyMap()
        } else {
            productRepository.findAvailableSizesByProductIds(productIds)
                .groupBy(
                    { it.getProductId() },
                    { it.getSize().name }
                )
        }

        val warehouseCities = if (productIds.isEmpty()) {
            emptyMap()
        } else {
            productRepository.findWarehouseCitiesByProductIds(productIds)
                .groupBy(
                    { it.getProductId() },
                    { it.getWarehouseCity() }
                )
        }

        return PageResponse(
            content = productPage.content.map { product ->
                toProductResponse(
                    product,
                    availableSizes[product.id].orEmpty(),
                    deliverTo,
                    warehouseCities[product.id].orEmpty()
                )
            },
            page = productPage.number,
            size = productPage.size,
            totalElements = productPage.totalElements,
            totalPages = productPage.totalPages,
            last = productPage.isLast
        )
    }

    fun getSimilarProducts(
        productId: Long,
        deliverTo: String?
    ): List<ProductResponse> {
        if (!productRepository.existsById(productId)) {
            throw ProductNotFoundException("Product not found")
        }

        val similarProducts = productRepository.findSimilarProducts(
            productId,
            SIMILAR_PRODUCT_PRICE_RANGE,
            PageRequest.of(
                0,
                SIMILAR_PRODUCT_LIMIT
            )
        ).ifEmpty {
            // Fall back to a wider price band only when the tight band returns nothing,
            // so categories with sparse price points still surface alternatives.
            productRepository.findSimilarProducts(
                productId,
                SIMILAR_PRODUCT_FALLBACK_PRICE_RANGE,
                PageRequest.of(
                    0,
                    SIMILAR_PRODUCT_LIMIT
                )
            )
        }

        val productIds = similarProducts.map { it.id }

        val availableSizes = if (productIds.isEmpty()) {
            emptyMap()
        } else {
            productRepository.findAvailableSizesByProductIds(productIds)
                .groupBy(
                    { it.getProductId() },
                    { it.getSize().name }
                )
        }

        val warehouseCities = if (productIds.isEmpty()) {
            emptyMap()
        } else {
            productRepository.findWarehouseCitiesByProductIds(productIds)
                .groupBy(
                    { it.getProductId() },
                    { it.getWarehouseCity() }
                )
        }

        return similarProducts.map { product ->
            toProductResponse(
                product,
                availableSizes[product.id].orEmpty(),
                deliverTo,
                warehouseCities[product.id].orEmpty()
            )
        }
    }

    private fun toProductResponse(
        product: Product,
        availableSizes: List<String>,
        deliverTo: String?,
        warehouseCities: List<String>
    ): ProductResponse {
        val hasStock = availableSizes.isNotEmpty()

        return ProductResponse(
            id = product.id,
            name = product.name,
            category = product.category,
            price = product.price,
            imageUrl = product.imageUrl,
            estimatedDelivery = deliverTo?.let {
                deliveryEstimator.estimateEarliestDelivery(
                    it,
                    warehouseCities
                )
            },
            estimatedDeliveryRange = if (deliverTo == null && hasStock) {
                deliveryEstimator.estimateDeliveryRange()
            } else {
                null
            },
            availableSizes = orderSizes(availableSizes)
        )
    }

    private fun orderSizes(
        sizes: List<String>
    ): List<String> =
        ProductSize.allValues()
            .filter { it in sizes.toSet() }

    private fun validateFilters(
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        size: String?,
        page: Int,
        pageSize: Int
    ) {
        if (minPrice != null && minPrice < BigDecimal.ZERO) {
            throw InvalidProductFilterException("minPrice cannot be negative")
        }

        if (maxPrice != null && maxPrice < BigDecimal.ZERO) {
            throw InvalidProductFilterException("maxPrice cannot be negative")
        }

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw InvalidProductFilterException("minPrice cannot be greater than maxPrice")
        }

        if (size != null && !ProductSize.isSupported(size)) {
            throw InvalidProductFilterException("Unsupported size: $size")
        }

        if (page < 0) {
            throw InvalidProductFilterException("page cannot be negative")
        }

        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw InvalidProductFilterException("size must be between 1 and $MAX_PAGE_SIZE")
        }
    }

    fun getProductDetails(
        productId: Long,
        deliverTo: String?
    ): ProductDetailsResponse {
        val rows = productRepository.findProductDetails(productId)

        if (rows.isEmpty()) {
            throw ProductNotFoundException("Product not found")
        }

        val product = rows.first()
        val sizes = buildSizeOptions(
            rows,
            deliverTo
        )

        val productPrice = product.getPrice()

        val platformFee = pricingProperties.platformFee

        val deliveryFee = pricingProperties.deliveryFee

        // GST in Pakistan applies to the product value only.
        // Platform and delivery fees carry separate tax treatment at the intermediary level.
        val vat = productPrice.multiply(pricingProperties.vatRate)

        val total = productPrice
            .add(platformFee)
            .add(deliveryFee)
            .add(vat)

        return ProductDetailsResponse(
            id = product.getId(),
            name = product.getName(),
            category = product.getCategory(),
            price = product.getPrice(),
            imageUrl = product.getImageUrl(),
            estimatedDelivery = if (deliverTo == null) {
                null
            } else {
                earliestDeliveryFromSizes(sizes)
            },
            estimatedDeliveryRange = if (deliverTo == null && sizes.any { it.available }) {
                deliveryEstimator.estimateDeliveryRange()
            } else {
                null
            },
            sizes = sizes,
            priceBreakdown = PriceBreakdownResponse(
                productPrice = productPrice,
                platformFee = platformFee,
                deliveryFee = deliveryFee,
                vat = vat,
                total = total
            )
        )
    }

    private fun buildSizeOptions(
        rows: List<ProductDetailsProjection>,
        deliverTo: String?
    ): List<SizeOptionResponse> =
        ProductSize.allValues().map { sizeName ->
            val size = ProductSize.valueOf(sizeName)
            val stockRows = rows.filter { row ->
                row.getSize() == size &&
                    row.getQuantity().orZero() > 0
            }

            val available = stockRows.isNotEmpty()
            val deliveryRange = if (available && deliverTo == null) {
                deliveryEstimator.estimateDeliveryRange()
            } else {
                null
            }
            val estimatedDelivery = if (!available || deliverTo == null) {
                null
            } else {
                estimateDeliveryForStock(
                    deliverTo,
                    stockRows
                )
            }

            SizeOptionResponse(
                size = sizeName,
                available = available,
                estimatedDelivery = estimatedDelivery,
                estimatedDeliveryRange = deliveryRange
            )
        }

    private fun estimateDeliveryForStock(
        deliverTo: String,
        stockRows: List<ProductDetailsProjection>
    ): LocalDate {
        val localStock = stockRows.firstOrNull { row ->
            row.getWarehouseCity() == deliverTo
        }

        val warehouseCity = localStock?.getWarehouseCity()
            ?: stockRows.first().getWarehouseCity()

        return deliveryEstimator.estimateDelivery(
            deliverTo,
            warehouseCity
        )
    }

    private fun earliestDeliveryFromSizes(
        sizes: List<SizeOptionResponse>
    ): LocalDate? =
        sizes.filter { it.available }
            .mapNotNull { it.estimatedDelivery }
            .minOrNull()

    companion object {
        private val SIMILAR_PRODUCT_PRICE_RANGE = BigDecimal("1000")
        private val SIMILAR_PRODUCT_FALLBACK_PRICE_RANGE = BigDecimal("3000")
        private const val SIMILAR_PRODUCT_LIMIT = 4
        private const val MAX_PAGE_SIZE = 50
    }
}

private fun Int?.orZero(): Int = this ?: 0
