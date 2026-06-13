package com.example.smartPurchase.product.repository

import com.example.smartPurchase.product.entity.ProductSize

interface ProductSizeProjection {

    fun getProductId(): Long

    fun getSize(): ProductSize
}
