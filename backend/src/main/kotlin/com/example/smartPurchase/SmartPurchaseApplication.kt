package com.example.smartPurchase

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SmartPurchaseApplication

fun main(args: Array<String>) {
	runApplication<SmartPurchaseApplication>(*args)
}
