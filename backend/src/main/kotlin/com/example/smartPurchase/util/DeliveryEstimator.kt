package com.example.smartPurchase.util

import com.example.smartPurchase.product.dto.DeliveryRangeResponse
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DeliveryEstimator {

    fun estimateDelivery(
        deliverTo: String? = null,
        warehouseCity: String? = null
    ): LocalDate {
        val days = when {
            deliverTo == null || warehouseCity == null -> BASE_DELIVERY_DAYS
            warehouseCity == deliverTo -> BASE_DELIVERY_DAYS
            else -> BASE_DELIVERY_DAYS + CROSS_CITY_EXTRA_DAYS
        }

        return LocalDate.now().plusDays(days)
    }

    fun estimateEarliestDelivery(
        deliverTo: String?,
        warehouseCities: Collection<String>
    ): LocalDate? {
        if (deliverTo == null || warehouseCities.isEmpty()) {
            return null
        }

        if (warehouseCities.any { it == deliverTo }) {
            return estimateDelivery(
                deliverTo,
                deliverTo
            )
        }

        return estimateDelivery(
            deliverTo,
            warehouseCities.first()
        )
    }

    fun estimateDeliveryRange(): DeliveryRangeResponse =
        DeliveryRangeResponse(
            from = LocalDate.now().plusDays(BASE_DELIVERY_DAYS),
            to = LocalDate.now().plusDays(
                BASE_DELIVERY_DAYS + CROSS_CITY_EXTRA_DAYS
            )
        )

    companion object {
        const val BASE_DELIVERY_DAYS = 5L
        const val CROSS_CITY_EXTRA_DAYS = 3L
    }
}
