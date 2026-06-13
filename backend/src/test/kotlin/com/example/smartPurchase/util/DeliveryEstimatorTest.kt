package com.example.smartPurchase.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DeliveryEstimatorTest {

    private val deliveryEstimator = DeliveryEstimator()

    @Test
    fun `should estimate delivery as five days from today`() {

        val expectedDeliveryDate =
            LocalDate.now().plusDays(5)

        val estimatedDeliveryDate =
            deliveryEstimator.estimateDelivery()

        assertEquals(
            expectedDeliveryDate,
            estimatedDeliveryDate
        )
    }
}
