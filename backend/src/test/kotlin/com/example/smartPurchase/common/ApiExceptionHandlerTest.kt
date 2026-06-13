package com.example.smartPurchase.common

import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest

class ApiExceptionHandlerTest {

    private val apiExceptionHandler =
        ApiExceptionHandler()

    @Test
    fun `should return structured response for unexpected errors`() {

        val request: HttpServletRequest =
            MockHttpServletRequest(
                "GET",
                "/products"
            )

        val response =
            apiExceptionHandler.handleUnexpectedError(
                RuntimeException("database connection failed"),
                request
            )

        assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            response.statusCode
        )

        val body =
            response.body!!

        assertEquals(500, body.status)
        assertEquals("Internal Server Error", body.error)
        assertEquals("Something went wrong", body.message)
        assertEquals("/products", body.path)
    }
}
