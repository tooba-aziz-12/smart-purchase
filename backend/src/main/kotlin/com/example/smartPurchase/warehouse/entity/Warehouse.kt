package com.example.smartPurchase.warehouse.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "warehouses")
data class Warehouse(

    @Id
    val id: Long = 0,

    val city: String = ""
)