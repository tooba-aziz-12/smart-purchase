package com.example.smartPurchase.product.repository

import com.example.smartPurchase.product.entity.ProductSize
import java.math.BigDecimal

interface ProductDetailsProjection {

    fun getId(): Long

    fun getName(): String

    fun getCategory(): String

    fun getPrice(): BigDecimal

    fun getSize(): ProductSize?

    fun getQuantity(): Int?
}