package com.panacea.rougeradar

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.DateTimeException

internal fun readAttendance(value: String?): Map<String, RemoteEvent> {
    val records = try { JSONArray(value ?: "[]") } catch (_: JSONException) { return emptyMap() }
    return (0 until records.length()).mapNotNull { index ->
        val item = records.optJSONObject(index) ?: return@mapNotNull null
        try {
            EventApiClient.parseEvent(item)
        } catch (_: JSONException) {
            null
        } catch (_: DateTimeException) {
            null
        }
    }.associateBy { it.id }
}

internal fun writeAttendance(events: Collection<RemoteEvent>): String = JSONArray().apply {
    events.forEach { event ->
        put(JSONObject().apply {
            put("id", event.id)
            put("title", event.title)
            put("category", event.category)
            put("venue", event.venue)
            put("startLocal", event.startLocal)
            put("endLocal", event.endLocal)
            put("timezone", event.timezone)
            put("timeType", event.timeType)
            put("address", event.address)
            put("description", event.description)
            put("priceMinUsd", event.priceMinUsd)
            put("ticketUrl", event.ticketUrl)
            put("ageRestriction", event.ageRestriction)
            put("latitude", event.latitude)
            put("longitude", event.longitude)
            put("sourceUrls", JSONArray(event.sourceUrls))
            put("checkedDate", event.checkedDate)
            put("sourceName", event.sourceName)
            put("listingUrl", event.listingUrl)
            put("imageUrl", event.imageUrl)
        })
    }
}.toString()
