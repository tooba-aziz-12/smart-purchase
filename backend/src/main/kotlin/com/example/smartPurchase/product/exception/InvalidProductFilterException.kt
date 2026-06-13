package com.example.smartPurchase.product.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidProductFilterException(
    message: String
) : IllegalArgumentException(message)
