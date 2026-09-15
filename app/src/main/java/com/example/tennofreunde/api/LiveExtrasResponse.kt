package com.example.tennofreunde.api

data class DailyDealResponse(
    val item: String?,
    val expiry: String?,
    val originalPrice: Int?,
    val salePrice: Int?,
    val total: Int?,
    val sold: Int?,
    val discount: Int?
)

data class NewsResponse(
    val message: String?,
    val date: String?,
    val translations: Map<String, String>?
)
