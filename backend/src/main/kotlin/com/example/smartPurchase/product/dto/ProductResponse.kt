package com.example.smartPurchase.product.dto

import java.math.BigDecimal
import java.time.LocalDate

data class ProductResponse(
    val id: Long,
    val name: String,
    val category: String,
    val price: BigDecimal,
    val imageUrl: String,
    val estimatedDelivery: LocalDate,
    val availableSizes: List<String>
)