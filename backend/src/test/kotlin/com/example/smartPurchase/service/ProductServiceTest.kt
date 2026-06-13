package com.example.smartPurchase.service

import com.example.smartPurchase.product.repository.ProductDetailsProjection
import com.example.smartPurchase.product.repository.ProductRepository
import com.example.smartPurchase.product.repository.ProductSearchProjection
import com.example.smartPurchase.product.service.ProductService
import com.example.smartPurchase.util.DeliveryEstimator
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class ProductServiceTest {

    @MockK
    lateinit var productRepository: ProductRepository

    @MockK
    lateinit var deliveryEstimator: DeliveryEstimator

    @InjectMockKs
    lateinit var productService: ProductService



    @Test
    fun `should return products`() {

        val projection = object : ProductSearchProjection {

            override fun getId() = 1L

            override fun getName() = "Blue Lawn Suit"

            override fun getCategory() = "Lawn"

            override fun getPrice() = BigDecimal("7500")

            override fun getAvailableSizes() =
                "M,L"
        }

        every {
            productRepository.search(
                null,
                null,
                null,
                null,
                null
            )
        } returns listOf(projection)

        every {
            deliveryEstimator.estimateDelivery()
        } returns LocalDate.now().plusDays(3)

        val result = productService.getProducts(
            null,
            null,
            null,
            null,
            null
        )

        assertEquals(1, result.size)
        assertEquals("Blue Lawn Suit", result[0].name)
        assertEquals(listOf("M", "L"), result[0].availableSizes)
    }

    @Test
    fun `should mark unavailable sizes correctly`() {

        val medium = object : ProductDetailsProjection {

            override fun getId() = 1L

            override fun getName() = "Blue Lawn Suit"

            override fun getCategory() = "Lawn"

            override fun getPrice() =
                BigDecimal("7500")

            override fun getSize() = "M"

            override fun getQuantity() = 5
        }

        val large = object : ProductDetailsProjection {

            override fun getId() = 1L

            override fun getName() = "Blue Lawn Suit"

            override fun getCategory() = "Lawn"

            override fun getPrice() =
                BigDecimal("7500")

            override fun getSize() = "L"

            override fun getQuantity() = 2
        }

        every {
            productRepository.findProductDetails(1)
        } returns listOf(
            medium,
            large
        )

        every {
            deliveryEstimator.estimateDelivery()
        } returns LocalDate.now().plusDays(3)

        val result =
            productService.getProductDetails(1)

        assertFalse(
            result.sizes.first {
                it.size == "S"
            }.available
        )

        assertTrue(
            result.sizes.first {
                it.size == "M"
            }.available
        )

        assertTrue(
            result.sizes.first {
                it.size == "L"
            }.available
        )
    }

    @Test
    fun `should calculate price breakdown correctly`() {

        val medium = object : ProductDetailsProjection {

            override fun getId() = 1L

            override fun getName() = "Blue Lawn Suit"

            override fun getCategory() = "Lawn"

            override fun getPrice() =
                BigDecimal("7500")

            override fun getSize() = "M"

            override fun getQuantity() = 5
        }

        every {
            productRepository.findProductDetails(1)
        } returns listOf(medium)

        every {
            deliveryEstimator.estimateDelivery()
        } returns LocalDate.now().plusDays(3)

        val result =
            productService.getProductDetails(1)

        assertEquals(
            BigDecimal("200"),
            result.priceBreakdown.platformFee
        )

        assertEquals(
            BigDecimal("250"),
            result.priceBreakdown.deliveryFee
        )

        assertEquals(
            BigDecimal("1192.50"),
            result.priceBreakdown.vat
        )

        assertEquals(
            BigDecimal("9142.50"),
            result.priceBreakdown.total
        )
    }

    @Test
    fun `should throw when product not found`() {

        every {
            productRepository.findProductDetails(999)
        } returns emptyList()

        assertThrows<IllegalArgumentException> {
            productService.getProductDetails(999)
        }
    }
}
