package com.example.smartPurchase.product.dto

import java.math.BigDecimal
import java.time.LocalDate

data class ProductDetailsResponse(
    val id: Long,
    val name: String,
    val category: String,
    val price: BigDecimal,
    val estimatedDelivery: LocalDate,
    val sizes: List<SizeOptionResponse>,
    val priceBreakdown: PriceBreakdownResponse
)