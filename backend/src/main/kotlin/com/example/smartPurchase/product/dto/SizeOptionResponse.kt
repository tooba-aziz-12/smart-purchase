package com.example.smartPurchase.product.dto

import java.time.LocalDate

data class SizeOptionResponse(
    val size: String,
    val available: Boolean,
    val estimatedDelivery: LocalDate? = null,
    val estimatedDeliveryRange: DeliveryRangeResponse? = null
)