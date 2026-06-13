package com.example.smartPurchase.product.repository

import java.math.BigDecimal
import java.time.LocalDate

interface ProductDetailsProjection {

    fun getId(): Long

    fun getName(): String

    fun getCategory(): String

    fun getPrice(): BigDecimal

    fun getSize(): String

    fun getQuantity(): Int
}