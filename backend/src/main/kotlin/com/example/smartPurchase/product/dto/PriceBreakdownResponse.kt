package com.example.smartPurchase.product.dto

import java.math.BigDecimal

data class PriceBreakdownResponse(
    val productPrice: BigDecimal,
    val platformFee: BigDecimal,
    val deliveryFee: BigDecimal,
    val vat: BigDecimal,
    val total: BigDecimal
)