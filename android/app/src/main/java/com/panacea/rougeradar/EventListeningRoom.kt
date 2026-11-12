package com.panacea.rougeradar

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

data class EventTrack(val title: String, val artist: String, val videoId: String, val isMoodMatch: Boolean = false) {
    init { require(videoId.matches(Regex("[A-Za-z0-9_-]{11}"))) }
    val watchUrl get() = "https://www.youtube.com/watch?v=$videoId"
}

@Composable
fun EventListeningRoom(track: EventTrack) {
    val context = LocalContext.current
    var expanded by remember(track) { mutableStateOf(false) }
    var noHandler by remember(track) { mutableStateOf(false) }
    DisposableEffect(context, track) {
        val lifecycle = (context as? ComponentActivity)?.lifecycle
        val observer = object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) { expanded = false }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("♫  THE LISTENING ROOM", style = MaterialTheme.typography.labelLarge)
            Text(track.title, style = MaterialTheme.typography.titleLarge)
            Text(track.artist, style = MaterialTheme.typography.bodyMedium)
            if (track.isMoodMatch) Text("A jazz mood match for this fictional lineup.", style = MaterialTheme.typography.bodySmall)
            if (expanded) {
                val appOrigin = "https://${context.packageName}"
                AndroidView(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp).aspectRatio(16f / 9f),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setBackgroundColor(Color.BLACK)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
                            settings.mediaPlaybackRequiresUserGesture = true
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                                    request.isForMainFrame || request.url.scheme != "https"
                            }
                            loadDataWithBaseURL(appOrigin, """
                                <!doctype html><html><head>
                                <meta name="viewport" content="width=device-width,initial-scale=1">
                                <meta name="referrer" content="strict-origin-when-cross-origin">
                                <style>html,body,iframe{margin:0;width:100%;height:100%;border:0;background:#000;display:block}</style>
                                </head><body><iframe title="YouTube music preview"
                                src="https://www.youtube.com/embed/${track.videoId}?playsinline=1"
                                allow="encrypted-media; picture-in-picture"></iframe></body></html>
                            """.trimIndent(), "text/html", "UTF-8", null)
                        }
                    },
                    onRelease = { player -> player.stopLoading(); player.destroy() },
                    update = {}
                )
            }
            OutlinedButton(onClick = { expanded = !expanded }, shape = RectangleShape) {
                Text(if (expanded) "Close player" else "▶  Open in-app player")
            }
            TextButton(
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(track.watchUrl)))
                    } catch (_: ActivityNotFoundException) {
                        noHandler = true
                    }
                },
                shape = RectangleShape
            ) { Text("Watch on YouTube ↗") }
            if (noHandler) Text("No browser or YouTube app is available.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
