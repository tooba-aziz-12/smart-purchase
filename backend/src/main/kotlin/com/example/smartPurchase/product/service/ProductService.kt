package com.example.smartPurchase.product.service

import com.example.smartPurchase.product.dto.ProductResponse
import com.example.smartPurchase.product.repository.ProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductService(
    private val productRepository: ProductRepository
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
                deliveryDate = product.getDeliveryDate(),
                availableSizes = product.getAvailableSizes()
                    .split(",")
                    .map { it.trim() }
            )
        }
    }
}