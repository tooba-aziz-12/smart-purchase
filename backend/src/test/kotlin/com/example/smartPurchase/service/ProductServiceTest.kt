package com.example.smartPurchase.service

import com.example.smartPurchase.product.config.PricingProperties
import com.example.smartPurchase.product.entity.Product
import com.example.smartPurchase.product.entity.ProductSize
import com.example.smartPurchase.product.exception.InvalidProductFilterException
import com.example.smartPurchase.product.exception.ProductNotFoundException
import com.example.smartPurchase.product.repository.ProductSizeProjection
import com.example.smartPurchase.product.repository.ProductDetailsProjection
import com.example.smartPurchase.product.repository.ProductRepository
import com.example.smartPurchase.product.service.ProductService
import com.example.smartPurchase.util.DeliveryEstimator
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class ProductServiceTest {

    @MockK
    lateinit var productRepository: ProductRepository

    @MockK
    lateinit var deliveryEstimator: DeliveryEstimator

    lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productService = ProductService(
            productRepository,
            deliveryEstimator,
            PricingProperties()
        )
    }


    @Test
    fun `should return products`() {

        val product = Product(
            id = 1L,
            name = "Sky Blue Embroidered Lawn Suit",
            category = "Lawn",
            price = BigDecimal("7500"),
            imageUrl = "/products/Blue Lawn Suit.png"
        )

        val mediumSize = object : ProductSizeProjection {

            override fun getProductId() = 1L

            override fun getSize() = ProductSize.M
        }

        val largeSize = object : ProductSizeProjection {

            override fun getProductId() = 1L

            override fun getSize() = ProductSize.L
        }

        every {
            productRepository.findProducts(
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(
                    0,
                    12,
                    Sort.by("id").ascending()
                )
            )
        } returns PageImpl(listOf(product))

        every {
            productRepository.findAvailableSizesByProductIds(listOf(1L))
        } returns listOf(
            mediumSize,
            largeSize
        )

        every {
            deliveryEstimator.estimateDelivery()
        } returns LocalDate.now().plusDays(3)

        val result = productService.getProducts(
            null,
            null,
            null,
            null,
            null,
            0,
            12
        )

        assertEquals(1, result.content.size)
        assertEquals("Sky Blue Embroidered Lawn Suit", result.content[0].name)
        assertEquals("/products/Blue Lawn Suit.png", result.content[0].imageUrl)
        assertEquals(listOf("M", "L"), result.content[0].availableSizes)
    }

    @Test
    fun `should mark unavailable sizes correctly`() {

        val medium = object : ProductDetailsProjection {

            override fun getId() = 1L

            override fun getName() = "Sky Blue Embroidered Lawn Suit"

            override fun getCategory() = "Lawn"

            override fun getPrice() =
                BigDecimal("7500")

            override fun getImageUrl() = "/products/Blue Lawn Suit.png"

            override fun getSize() = ProductSize.M

            override fun getQuantity() = 5
        }

        val large = object : ProductDetailsProjection {

            override fun getId() = 1L

            override fun getName() = "Sky Blue Embroidered Lawn Suit"

            override fun getCategory() = "Lawn"

            override fun getPrice() =
                BigDecimal("7500")

            override fun getImageUrl() = "/products/Blue Lawn Suit.png"

            override fun getSize() = ProductSize.L

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

            override fun getName() = "Sky Blue Embroidered Lawn Suit"

            override fun getCategory() = "Lawn"

            override fun getPrice() =
                BigDecimal("7500")

            override fun getImageUrl() = "/products/Blue Lawn Suit.png"

            override fun getSize() = ProductSize.M

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
            BigDecimal("1125.00"),
            result.priceBreakdown.vat
        )

        assertEquals(
            BigDecimal("9075.00"),
            result.priceBreakdown.total
        )
    }

    @Test
    fun `should throw when product not found`() {

        every {
            productRepository.findProductDetails(999)
        } returns emptyList()

        assertThrows<ProductNotFoundException> {
            productService.getProductDetails(999)
        }
    }

    @Test
    fun `should return similar products`() {

        val detailsProjection = object : ProductDetailsProjection {

            override fun getId() = 1L

            override fun getName() = "Sky Blue Embroidered Lawn Suit"

            override fun getCategory() = "Lawn"

            override fun getPrice() =
                BigDecimal("7500")

            override fun getImageUrl() = "/products/Blue Lawn Suit.png"

            override fun getSize() = ProductSize.M

            override fun getQuantity() = 3
        }

        val similarProduct = Product(
            id = 2L,
            name = "Mint Green Embroidered Lawn Suit",
            category = "Lawn",
            price = BigDecimal("6900"),
            imageUrl = "/products/Green Lawn suit.png"
        )

        val mediumSize = object : ProductSizeProjection {

            override fun getProductId() = 2L

            override fun getSize() = ProductSize.M
        }

        every {
            productRepository.existsById(1)
        } returns true

        every {
            productRepository.findSimilarProducts(
                1,
                BigDecimal("1000"),
                PageRequest.of(
                    0,
                    4
                )
            )
        } returns listOf(similarProduct)

        every {
            productRepository.findAvailableSizesByProductIds(listOf(2L))
        } returns listOf(mediumSize)

        every {
            deliveryEstimator.estimateDelivery()
        } returns LocalDate.now().plusDays(3)

        val result =
            productService.getSimilarProducts(1)

        assertEquals(1, result.size)
        assertEquals(2L, result[0].id)
        assertEquals("Mint Green Embroidered Lawn Suit", result[0].name)
        assertEquals("/products/Green Lawn suit.png", result[0].imageUrl)
        assertEquals(listOf("M"), result[0].availableSizes)
    }

    @Test
    fun `should throw when minimum price is negative`() {
        assertThrows<InvalidProductFilterException> {
            productService.getProducts(null, BigDecimal("-1"), null, null, null, 0, 12)
        }
    }

    @Test
    fun `should throw when maximum price is negative`() {
        assertThrows<InvalidProductFilterException> {
            productService.getProducts(null, null, BigDecimal("-1"), null, null, 0, 12)
        }
    }

    @Test
    fun `should throw when minimum price exceeds maximum price`() {
        assertThrows<InvalidProductFilterException> {
            productService.getProducts(null, BigDecimal("9000"), BigDecimal("5000"), null, null, 0, 12)
        }
    }

    @Test
    fun `should throw when size is not supported`() {
        assertThrows<InvalidProductFilterException> {
            productService.getProducts(null, null, null, "XL", null, 0, 12)
        }
    }

    @Test
    fun `should throw when page is negative`() {
        assertThrows<InvalidProductFilterException> {
            productService.getProducts(null, null, null, null, null, -1, 12)
        }
    }

    @Test
    fun `should throw when page size is zero`() {
        assertThrows<InvalidProductFilterException> {
            productService.getProducts(null, null, null, null, null, 0, 0)
        }
    }

    @Test
    fun `should throw when page size exceeds maximum`() {
        assertThrows<InvalidProductFilterException> {
            productService.getProducts(null, null, null, null, null, 0, 51)
        }
    }

    @Test
    fun `should throw when finding similar products for unknown product`() {

        every {
            productRepository.existsById(999)
        } returns false

        assertThrows<ProductNotFoundException> {
            productService.getSimilarProducts(999)
        }
    }
}
