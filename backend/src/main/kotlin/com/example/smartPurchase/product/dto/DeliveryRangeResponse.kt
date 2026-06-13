package com.example.smartPurchase.product.dto

import java.time.LocalDate

data class DeliveryRangeResponse(
    val from: LocalDate,
    val to: LocalDate
)
