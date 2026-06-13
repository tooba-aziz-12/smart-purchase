package com.example.smartPurchase.controller

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


}