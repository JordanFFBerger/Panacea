package com.panacea.rougeradar

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private val downloads = Semaphore(3)
private val artworkCache = object : LruCache<String, ImageBitmap>(16 * 1024 * 1024) {
    override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4
}

internal suspend fun loadEventArtwork(event: RemoteEvent): ImageBitmap? =
    withContext(Dispatchers.IO) {
        artworkCache.get(event.id)?.let { return@withContext it }
        downloads.withPermit {
            try {
                val url = event.imageUrl ?: communityArtworkUrl(event) ?: return@withPermit null
                val bytes = readArtworkResponse(url, 2 * 1024 * 1024) ?: return@withPermit null
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withPermit null
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1200) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size,
                    BitmapFactory.Options().apply { inSampleSize = sample })
                    ?.asImageBitmap()?.also { artworkCache.put(event.id, it) }
            } catch (_: IOException) {
                null
            } catch (_: org.json.JSONException) {
                null
            }
        }
    }

private fun communityArtworkUrl(event: RemoteEvent): String? {
    val listing = event.listingUrl ?: return null
    val occurrence = event.id.takeIf { it.startsWith("hasthings:") }?.substringAfter(':') ?: return null
    val slug = Regex("""https://baton-rouge\.hasthings\.com/events/([a-z0-9-]+)""")
        .matchEntire(listing)?.groupValues?.get(1) ?: return null
    val bytes = readArtworkResponse("https://baton-rouge.hasthings.com/api/events/$slug", 512 * 1024)
        ?: return null
    val item = JSONObject(bytes.toString(Charsets.UTF_8)).optJSONObject("item") ?: return null
    val occurrences = item.optJSONArray("occurrences") ?: return null
    val matches = (0 until occurrences.length()).any {
        occurrences.optJSONObject(it)?.optString("id") == occurrence
    }
    return if (matches && !item.isNull("imageUrl")) item.optString("imageUrl") else null
}

private fun readArtworkResponse(address: String, limit: Int): ByteArray? {
    if (!address.startsWith("https://")) return null
    val connection = URL(address).openConnection() as HttpsURLConnection
    return try {
        connection.connectTimeout = 4000
        connection.readTimeout = 4000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("User-Agent", "Panacea Android")
        if (connection.responseCode != 200 || connection.contentLength > limit) return null
        connection.inputStream.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                if (output.size() + count > limit) return null
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    } finally {
        connection.disconnect()
    }
}
