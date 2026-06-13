package com.example.smartPurchase.product.service

import com.example.smartPurchase.product.dto.PriceBreakdownResponse
import com.example.smartPurchase.product.dto.ProductDetailsResponse
import com.example.smartPurchase.product.dto.ProductResponse
import com.example.smartPurchase.product.dto.SizeOptionResponse
import com.example.smartPurchase.product.exception.ProductNotFoundException
import com.example.smartPurchase.product.repository.ProductRepository
import com.example.smartPurchase.util.DeliveryEstimator
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val deliveryEstimator: DeliveryEstimator

) {

    fun getProducts(
        category: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        size: String?,
        city: String?
    ): List<ProductResponse> {
        return productRepository.search(
            category,
            minPrice,
            maxPrice,
            size,
            city
        ).map { product ->

            ProductResponse(
                id = product.getId(),
                name = product.getName(),
                category = product.getCategory(),
                price = product.getPrice(),
                deliveryDate = deliveryEstimator.estimateDelivery(),
                availableSizes = product.getAvailableSizes()
                    .split(",")
                    .map { it.trim() }
            )
        }
    }
    fun getProductDetails(productId: Long): ProductDetailsResponse {
        val rows = productRepository.findProductDetails(productId)

        if (rows.isEmpty()) {
            throw ProductNotFoundException("Product not found")
        }

        val product = rows.first()

        val availableSizes = rows
            .filter { it.getQuantity() > 0 }
            .map { it.getSize() }
            .toSet()

        val allSizes = listOf("S", "M", "L")

        val productPrice = product.getPrice()

        val platformFee = BigDecimal("200")

        val deliveryFee = BigDecimal("250")

        val vat = (
                productPrice
                    .add(platformFee)
                    .add(deliveryFee)
                )
            .multiply(BigDecimal("0.15"))

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
}