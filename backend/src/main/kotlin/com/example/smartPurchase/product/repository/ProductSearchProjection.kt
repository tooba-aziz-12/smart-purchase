package com.example.smartPurchase.product.repository

import java.math.BigDecimal
import java.time.LocalDate

interface ProductSearchProjection {

    fun getId(): Long

    fun getName(): String

    fun getCategory(): String

    fun getPrice(): BigDecimal

    fun getDeliveryDate(): LocalDate

    fun getAvailableSizes(): String
}