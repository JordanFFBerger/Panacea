
package com.panacea.rougeradar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime

object EventApiClient {

    private const val BASE_URL = BuildConfig.EVENT_API_URL

    suspend fun fetchEvents(): List<RemoteEvent> =
        withContext(Dispatchers.IO) {
            if (BASE_URL.isBlank()) throw IOException("No event API configured")

            val connection = (
                    URL("$BASE_URL/api/events").openConnection()
                            as HttpURLConnection
                    ).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Accept", "application/json")
                }

            try {
                if (connection.responseCode != 200) {
                    throw IOException(
                        "API returned HTTP ${connection.responseCode}"
                    )
                }

                val json = connection.inputStream
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }

                val response = JSONObject(json)
                val items = response.getJSONArray("events")

                if (response.getInt("count") != items.length()) {
                    throw IOException("Event count mismatch")
                }

                List(items.length()) { index ->
                    parseEvent(items.getJSONObject(index))
                }

            } finally {
                connection.disconnect()
            }
        }

    suspend fun fetchEvent(id: String): RemoteEvent = withContext(Dispatchers.IO) {
        val path = android.net.Uri.encode(id)
        val connection = URL("$BASE_URL/api/events/$path").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val json = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseEvent(JSONObject(json).getJSONObject("event"))
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseEvent(item: JSONObject): RemoteEvent {

        fun optionalText(key: String): String? =
            if (item.isNull(key)) null
            else item.getString(key)

        fun optionalNumber(key: String): Double? =
            if (item.isNull(key)) null
            else item.getDouble(key)

        val startLocal = item.getString("startLocal")
        OffsetDateTime.parse(startLocal)

        val sources = item.getJSONArray("sourceUrls")
        val sourceUrls = List(sources.length()) {
            sources.getString(it)
        }

        return RemoteEvent(
            id = item.getString("id"),
            title = item.getString("title"),
            category = item.getString("category"),
            venue = item.getString("venue"),
            startLocal = startLocal,
            endLocal = optionalText("endLocal"),
            timezone = item.getString("timezone"),
            timeType = item.getString("timeType"),
            address = optionalText("address"),
            description = optionalText("description"),
            priceMinUsd = optionalNumber("priceMinUsd"),
            ticketUrl = optionalText("ticketUrl"),
            ageRestriction = optionalText("ageRestriction"),
            latitude = optionalNumber("latitude"),
            longitude = optionalNumber("longitude"),
            sourceUrls = sourceUrls,
            checkedDate = optionalText("checkedDate"),
            sourceName = optionalText("sourceName"),
            listingUrl = optionalText("listingUrl"),
            imageUrl = optionalText("imageUrl")
        )
    }
}
