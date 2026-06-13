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
import com.example.smartPurchase.product.repository.ProductRepository
import com.example.smartPurchase.util.DeliveryEstimator
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal

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
        city: String?,
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

        val productPage = productRepository.search(
            category,
            minPrice,
            maxPrice,
            parsedSize,
            city,
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

        return PageResponse(
            content = productPage.content.map { product ->
                toProductResponse(
                    product,
                    availableSizes[product.id].orEmpty()
                )
            },
            page = productPage.number,
            size = productPage.size,
            totalElements = productPage.totalElements,
            totalPages = productPage.totalPages,
            last = productPage.isLast
        )
    }

    fun getSimilarProducts(productId: Long): List<ProductResponse> {
        if (productRepository.findProductDetails(productId).isEmpty()) {
            throw ProductNotFoundException("Product not found")
        }

        val similarProducts = productRepository.findSimilarProducts(
            productId,
            SIMILAR_PRODUCT_PRICE_RANGE,
            PageRequest.of(
                0,
                SIMILAR_PRODUCT_LIMIT
            )
        )

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

        return similarProducts.map { product ->
            toProductResponse(
                product,
                availableSizes[product.id].orEmpty()
            )
        }
    }

    private fun toProductResponse(
        product: Product,
        availableSizes: List<String>
    ): ProductResponse =
        ProductResponse(
            id = product.id,
            name = product.name,
            category = product.category,
            price = product.price,
            estimatedDelivery = deliveryEstimator.estimateDelivery(),
            availableSizes = orderSizes(availableSizes)
        )

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

    fun getProductDetails(productId: Long): ProductDetailsResponse {
        val rows = productRepository.findProductDetails(productId)

        if (rows.isEmpty()) {
            throw ProductNotFoundException("Product not found")
        }

        val product = rows.first()

        val availableSizes = rows
            .mapNotNull { row ->
                row.getSize()
                    ?.takeIf { row.getQuantity().orZero() > 0 }
                    ?.name
            }
            .toSet()

        val allSizes = ProductSize.allValues()

        val productPrice = product.getPrice()

        val platformFee = pricingProperties.platformFee

        val deliveryFee = pricingProperties.deliveryFee

        val vat = (
                productPrice
                    .add(platformFee)
                    .add(deliveryFee)
                )
            .multiply(pricingProperties.vatRate)

        val total = productPrice
            .add(platformFee)
            .add(deliveryFee)
            .add(vat)

        return ProductDetailsResponse(
            id = product.getId(),
            name = product.getName(),
            category = product.getCategory(),
            price = product.getPrice(),
            estimatedDelivery = deliveryEstimator.estimateDelivery(),
            sizes = allSizes.map { size ->
                SizeOptionResponse(
                    size = size,
                    available = availableSizes.contains(size)
                )
            },
            priceBreakdown = PriceBreakdownResponse(
                productPrice = productPrice,
                platformFee = platformFee,
                deliveryFee = deliveryFee,
                vat = vat,
                total = total
            )
        )
    }

    companion object {
        private val SIMILAR_PRODUCT_PRICE_RANGE = BigDecimal("1000")
        private const val SIMILAR_PRODUCT_LIMIT = 4
        private const val MAX_PAGE_SIZE = 50
    }
}

private fun Int?.orZero(): Int = this ?: 0