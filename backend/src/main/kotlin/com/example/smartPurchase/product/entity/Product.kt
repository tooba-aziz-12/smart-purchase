package com.example.smartPurchase.product.entity

import java.math.BigDecimal
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "products")
data class Product(

    @Id
    val id: Long = 0,

    val name: String = "",

    val category: String = "",

    val price: BigDecimal = BigDecimal.ZERO,

    val imageUrl: String = ""
)