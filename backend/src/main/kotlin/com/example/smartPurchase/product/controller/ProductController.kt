package com.example.smartPurchase.product.controller

import com.example.smartPurchase.product.dto.ProductDetailsResponse
import com.example.smartPurchase.product.dto.ProductResponse
import com.example.smartPurchase.product.service.ProductService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    fun getProducts(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) size: String?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?
    ): List<ProductResponse> =
        productService.getProducts(
            category,
            minPrice,
            maxPrice,
            size,
            city
        )

    @GetMapping("/{id}")
    fun getProductDetails(
        @PathVariable id: Long
    ): ProductDetailsResponse =
        productService.getProductDetails(id)

    @GetMapping("/{id}/similar")
    fun getSimilarProducts(
        @PathVariable id: Long
    ): List<ProductResponse> =
        productService.getSimilarProducts(id)
}