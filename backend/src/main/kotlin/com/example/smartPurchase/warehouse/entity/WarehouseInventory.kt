package com.example.smartPurchase.warehouse.entity

import com.example.smartPurchase.product.entity.Product
import com.example.smartPurchase.product.entity.ProductSize
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "warehouse_inventory",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_warehouse_inventory_product_size",
            columnNames = ["warehouse_id", "product_id", "size"]
        )
    ]
)
data class WarehouseInventory(

    @Id
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    val warehouse: Warehouse = Warehouse(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product = Product(),

    @Enumerated(EnumType.STRING)
    val size: ProductSize = ProductSize.S,

    val quantity: Int = 0
)
