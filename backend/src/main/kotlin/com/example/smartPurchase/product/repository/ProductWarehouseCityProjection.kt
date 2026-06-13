package com.example.smartPurchase.product.repository

interface ProductWarehouseCityProjection {

    fun getProductId(): Long

    fun getWarehouseCity(): String
}
