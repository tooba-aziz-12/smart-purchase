package com.example.smartPurchase.controller

import com.example.smartPurchase.common.dto.ErrorResponse
import com.example.smartPurchase.product.dto.ProductDetailsResponse
import com.example.smartPurchase.product.dto.ProductResponse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIT {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return all products`() {

        val response = mockMvc.get("/products")
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertEquals(12, products.size)
    }

    @Test
    fun `should filter products by category`() {

        val response = mockMvc.get("/products") {
            param("category", "Lawn")
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertTrue(
            products.all {
                it.category == "Lawn"
            }
        )
    }

    @Test
    fun `should filter products by city`() {

        val response = mockMvc.get("/products") {
            param("city", "Lahore")
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertTrue(products.isNotEmpty())
    }

    @Test
    fun `should filter products by size`() {

        val response = mockMvc.get("/products") {
            param("size", "L")
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertTrue(products.isNotEmpty())

        assertTrue(
            products.all {
                "L" in it.availableSizes
            }
        )
    }

    @Test
    fun `should filter products by minimum price`() {

        val response = mockMvc.get("/products") {
            param("minPrice", "9000")
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertTrue(products.isNotEmpty())

        assertTrue(
            products.all {
                it.price >= BigDecimal("9000")
            }
        )
    }

    @Test
    fun `should filter products by maximum price`() {

        val response = mockMvc.get("/products") {
            param("maxPrice", "6000")
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertTrue(products.isNotEmpty())

        assertTrue(
            products.all {
                it.price <= BigDecimal("6000")
            }
        )
    }

    @Test
    fun `should filter products by price range`() {

        val response = mockMvc.get("/products") {
            param("minPrice", "7000")
            param("maxPrice", "8000")
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertTrue(products.isNotEmpty())

        assertTrue(
            products.all {
                it.price >= BigDecimal("7000") &&
                    it.price <= BigDecimal("8000")
            }
        )
    }

    @Test
    fun `should reject negative minimum price`() {

        mockMvc.get("/products") {
            param("minPrice", "-1")
        }
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should reject negative maximum price`() {

        mockMvc.get("/products") {
            param("maxPrice", "-1")
        }
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should reject minimum price greater than maximum price`() {

        mockMvc.get("/products") {
            param("minPrice", "9000")
            param("maxPrice", "5000")
        }
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should reject unsupported size`() {

        mockMvc.get("/products") {
            param("size", "XL")
        }
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should return empty results for unknown category`() {

        val response = mockMvc.get("/products") {
            param("category", "Shoes")
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertTrue(products.isEmpty())
    }

    @Test
    fun `should return empty results for unknown city`() {

        val response = mockMvc.get("/products") {
            param("city", "Multan")
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertTrue(products.isEmpty())
    }

    @Test
    fun `should return product details`() {

        val response = mockMvc.get("/products/1")
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val product =
            objectMapper.readValue(
                response.response.contentAsString,
                ProductDetailsResponse::class.java
            )

        assertEquals(1L, product.id)
        assertEquals("Blue Lawn Suit", product.name)

        assertEquals(3, product.sizes.size)

        assertFalse(
            product.sizes.first {
                it.size == "S"
            }.available
        )

        assertTrue(
            product.sizes.first {
                it.size == "M"
            }.available
        )

        assertTrue(
            product.sizes.first {
                it.size == "L"
            }.available
        )
    }

    @Test
    fun `should return similar products`() {

        val response = mockMvc.get("/products/1/similar")
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val products: List<ProductResponse> =
            objectMapper.readValue(
                response.response.contentAsString,
                object : TypeReference<List<ProductResponse>>() {}
            )

        assertTrue(products.isNotEmpty())

        assertTrue(
            products.none {
                it.id == 1L
            }
        )

        assertTrue(
            products.all {
                it.category == "Lawn"
            }
        )

        assertTrue(
            products.all {
                it.price >= BigDecimal("6500") &&
                    it.price <= BigDecimal("8500")
            }
        )
    }

    @Test
    fun `should return not found for similar products of unknown product`() {

        mockMvc.get("/products/999/similar")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return not found for unknown product`() {

        val response = mockMvc.get("/products/999")
            .andExpect {
                status { isNotFound() }
            }
            .andReturn()

        val error =
            objectMapper.readValue(
                response.response.contentAsString,
                ErrorResponse::class.java
            )

        assertEquals(404, error.status)
        assertEquals("Product not found", error.message)
    }

}