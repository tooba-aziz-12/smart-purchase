package com.example.smartPurchase.common

import com.example.smartPurchase.common.dto.ErrorResponse
import com.example.smartPurchase.product.exception.InvalidProductFilterException
import com.example.smartPurchase.product.exception.ProductNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.OffsetDateTime

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFound(
        exception: ProductNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        errorResponse(
            HttpStatus.NOT_FOUND,
            exception.message ?: "Product not found",
            request
        )

    @ExceptionHandler(InvalidProductFilterException::class)
    fun handleInvalidProductFilter(
        exception: InvalidProductFilterException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        errorResponse(
            HttpStatus.BAD_REQUEST,
            exception.message ?: "Invalid product filter",
            request
        )

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        MethodArgumentNotValidException::class
    )
    fun handleBadRequest(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        errorResponse(
            HttpStatus.BAD_REQUEST,
            exception.message ?: "Invalid request",
            request
        )

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedError(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        errorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Something went wrong",
            request
        )

    private fun errorResponse(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    timestamp = OffsetDateTime.now(),
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message,
                    path = request.requestURI
                )
            )
}
