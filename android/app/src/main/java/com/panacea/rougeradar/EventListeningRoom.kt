package com.panacea.rougeradar

import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay
import java.util.Locale

data class EventTrack(val title: String, val artist: String, @RawRes val audioRes: Int, val sourceUrl: String)

private fun timeLabel(ms: Int): String {
    val seconds = ms / 1000
    return String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60)
}

@Composable
fun EventListeningRoom(track: EventTrack) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var player by remember(track) { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember(track) { mutableStateOf(false) }
    var elapsed by remember(track) { mutableIntStateOf(0) }
    var duration by remember(track) { mutableIntStateOf(0) }
    var seeking by remember(track) { mutableStateOf<Float?>(null) }
    var failed by remember(track) { mutableStateOf(false) }

    fun pause() {
        player?.let { p ->
            if (p.isPlaying) p.pause()
            elapsed = p.currentPosition
        }
        playing = false
    }

    DisposableEffect(context, track) {
        val lifecycle = (context as? ComponentActivity)?.lifecycle
        val observer = object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) { pause() }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
            player?.release()
        }
    }

    LaunchedEffect(playing, player) {
        val current = player ?: return@LaunchedEffect
        while (playing) {
            elapsed = current.currentPosition
            delay(200)
        }
    }

    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("♫  THE LISTENING ROOM", style = MaterialTheme.typography.labelLarge)
            Text(track.title, style = MaterialTheme.typography.titleLarge)
            Text(track.artist, style = MaterialTheme.typography.bodyMedium)
            Text("Jazz mood match for this fictional lineup · available offline", style = MaterialTheme.typography.bodySmall)
            if (duration > 0) {
                Slider(
                    value = seeking ?: elapsed.toFloat(),
                    onValueChange = { seeking = it },
                    onValueChangeFinished = {
                        seeking?.toInt()?.let { position ->
                            player?.seekTo(position)
                            elapsed = position
                        }
                        seeking = null
                    },
                    valueRange = 0f..duration.toFloat(),
                    modifier = Modifier.testTag("preview-progress")
                )
                Text("${timeLabel(seeking?.toInt() ?: elapsed)} / ${timeLabel(duration)}",
                    style = MaterialTheme.typography.labelSmall)
            }
            Button(onClick = {
                if (playing) {
                    pause()
                } else {
                    failed = false
                    try {
                        val current = player ?: checkNotNull(MediaPlayer.create(context, track.audioRes)).also { p ->
                            player = p
                            duration = p.duration
                            p.setOnCompletionListener { completed ->
                                playing = false
                                elapsed = 0
                                completed.seekTo(0)
                            }
                            p.setOnErrorListener { broken, _, _ ->
                                playing = false
                                failed = true
                                duration = 0
                                elapsed = 0
                                seeking = null
                                player = null
                                broken.release()
                                true
                            }
                        }
                        current.start()
                        playing = true
                    } catch (_: RuntimeException) {
                        player?.release()
                        player = null
                        duration = 0
                        elapsed = 0
                        failed = true
                    }
                }
            }, shape = RectangleShape) { Text(if (playing) "Pause preview" else "Play preview") }
            if (failed) Text("The preview could not play. Tap Play preview to retry.", style = MaterialTheme.typography.bodySmall)
            Row {
                TextButton(onClick = { uriHandler.openUri(track.sourceUrl) }, shape = RectangleShape) { Text("Incompetech ↗") }
                TextButton(onClick = { uriHandler.openUri("https://creativecommons.org/licenses/by/4.0/") },
                    shape = RectangleShape) { Text("CC BY 4.0") }
            }
        }
    }
}
