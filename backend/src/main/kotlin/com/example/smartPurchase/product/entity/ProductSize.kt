package com.example.smartPurchase.product.entity

enum class ProductSize {
    S,
    M,
    L;

    companion object {
        fun isSupported(value: String): Boolean =
            entries.any { it.name == value }

        fun allValues(): List<String> =
            entries.map { it.name }
    }
}
