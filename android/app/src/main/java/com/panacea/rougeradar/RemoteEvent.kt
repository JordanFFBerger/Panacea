 package com.panacea.rougeradar

data class RemoteEvent(
    val id: String,
    val title: String,
    val category: String,
    val venue: String,
    val startLocal: String,
    val endLocal: String?,
    val timezone: String,
    val timeType: String,
    val address: String?,
    val description: String?,
    val priceMinUsd: Double?,
    val ticketUrl: String?,
    val ageRestriction: String?,
    val latitude: Double?,
    val longitude: Double?,
    val sourceUrls: List<String>,
    val checkedDate: String?,
    val sourceName: String? = null,
    val listingUrl: String? = null
)
