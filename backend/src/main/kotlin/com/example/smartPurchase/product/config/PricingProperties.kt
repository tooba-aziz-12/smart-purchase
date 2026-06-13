package com.example.smartPurchase.product.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.math.BigDecimal

@ConfigurationProperties(prefix = "smart-purchase.pricing")
data class PricingProperties(
    val platformFee: BigDecimal = BigDecimal("200"),
    val deliveryFee: BigDecimal = BigDecimal("250"),
    val vatRate: BigDecimal = BigDecimal("0.15")
)
