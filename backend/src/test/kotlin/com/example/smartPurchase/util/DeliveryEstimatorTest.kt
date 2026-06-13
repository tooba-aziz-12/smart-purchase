package com.example.smartPurchase.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DeliveryEstimatorTest {

    private val deliveryEstimator = DeliveryEstimator()

    @Test
    fun `should estimate delivery as five days when deliver to city is not provided`() {

        val estimatedDeliveryDate =
            deliveryEstimator.estimateDelivery()

        assertEquals(
            LocalDate.now().plusDays(DeliveryEstimator.BASE_DELIVERY_DAYS),
            estimatedDeliveryDate
        )
    }

    @Test
    fun `should estimate delivery as five days for local warehouse fulfillment`() {

        val estimatedDeliveryDate =
            deliveryEstimator.estimateDelivery(
                deliverTo = "Lahore",
                warehouseCity = "Lahore"
            )

        assertEquals(
            LocalDate.now().plusDays(DeliveryEstimator.BASE_DELIVERY_DAYS),
            estimatedDeliveryDate
        )
    }

    @Test
    fun `should add extra days for cross city fulfillment`() {

        val estimatedDeliveryDate =
            deliveryEstimator.estimateDelivery(
                deliverTo = "Lahore",
                warehouseCity = "Karachi"
            )

        assertEquals(
            LocalDate.now().plusDays(
                DeliveryEstimator.BASE_DELIVERY_DAYS +
                    DeliveryEstimator.CROSS_CITY_EXTRA_DAYS
            ),
            estimatedDeliveryDate
        )
    }

    @Test
    fun `should use earliest delivery when local stock exists`() {

        val estimatedDeliveryDate =
            deliveryEstimator.estimateEarliestDelivery(
                deliverTo = "Lahore",
                warehouseCities = listOf("Karachi", "Lahore")
            )

        assertEquals(
            LocalDate.now().plusDays(DeliveryEstimator.BASE_DELIVERY_DAYS),
            estimatedDeliveryDate
        )
    }

    @Test
    fun `should estimate delivery range when deliver to city is not provided`() {

        val deliveryRange =
            deliveryEstimator.estimateDeliveryRange()

        assertEquals(
            LocalDate.now().plusDays(DeliveryEstimator.BASE_DELIVERY_DAYS),
            deliveryRange.from
        )

        assertEquals(
            LocalDate.now().plusDays(
                DeliveryEstimator.BASE_DELIVERY_DAYS +
                    DeliveryEstimator.CROSS_CITY_EXTRA_DAYS
            ),
            deliveryRange.to
        )
    }

    @Test
    fun `should return null delivery estimate when deliver to city is not provided`() {

        val estimatedDeliveryDate =
            deliveryEstimator.estimateEarliestDelivery(
                deliverTo = null,
                warehouseCities = listOf("Karachi", "Lahore")
            )

        assertEquals(
            null,
            estimatedDeliveryDate
        )
    }

    @Test
    fun `should use cross city delivery when only remote stock exists`() {

        val estimatedDeliveryDate =
            deliveryEstimator.estimateEarliestDelivery(
                deliverTo = "Lahore",
                warehouseCities = listOf("Karachi")
            )

        assertEquals(
            LocalDate.now().plusDays(
                DeliveryEstimator.BASE_DELIVERY_DAYS +
                    DeliveryEstimator.CROSS_CITY_EXTRA_DAYS
            ),
            estimatedDeliveryDate
        )
    }
}
