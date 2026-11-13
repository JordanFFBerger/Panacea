package com.panacea.rougeradar

import java.text.NumberFormat
import java.util.Locale
import java.time.OffsetDateTime
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Event(
    val id: Int, val title: String, val category: String, val venue: String,
    val daysAway: Long, val time: String, val price: Int, val activities: List<String>,
    val description: String, val signups: Int, val recent: Int, val previous: Int,
    val symbol: String, val miles: Double?, val latitude: Double?, val longitude: Double?,
    val track: EventTrack? = null,
    val listing: RemoteEvent? = null
) {
    val key: String get() = listing?.let { "catalog:${it.id}" } ?: id.toString()
    val isSample: Boolean get() = listing == null
    val trending: Boolean get() = isSample && recent >= 20 && recent >= previous * 1.5
    val amount: Double? get() = if (isSample) price.toDouble() else listing?.priceMinUsd
    val priceLabel: String get() = when (val value = amount) {
        null -> "Price not listed"
        0.0 -> "Free"
        else -> if (isSample) "$$price" else "From ${NumberFormat.getCurrencyInstance(Locale.US).format(value)}"
    }
    private val start = listing?.let { OffsetDateTime.parse(it.startLocal) }
    val date: LocalDate get() = start?.toLocalDate() ?: LocalDate.now(ZoneId.of("America/Chicago")).plusDays(daysAway)
    val dateLabel: String get() = date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
}
