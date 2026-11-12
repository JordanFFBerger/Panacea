package com.panacea.rougeradar

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun EventDetails(e:Event,saved:Boolean,joined:Boolean,onBack:()->Unit,onSave:()->Unit,onJoin:()->Unit) {
    val context=LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        TextButton(shape=androidx.compose.ui.graphics.RectangleShape,onClick=onBack) { Text("← Back to events") }
        EventCover(e, Modifier.fillMaxWidth().height(190.dp))
        PhotoCredits()
        Text(e.title,style=MaterialTheme.typography.headlineLarge)
        Text(e.priceLabel,style=MaterialTheme.typography.headlineSmall,color=MaterialTheme.colorScheme.primary)
        Text("${e.dateLabel} · ${e.time}")
        Text("◎ ${e.venue}")
        Text(e.description)
        e.track?.let { EventListeningRoom(it) }
        HorizontalDivider()
        Text("What's included",style=MaterialTheme.typography.titleMedium)
        e.activities.forEach { Text("✓ $it") }
        Surface(color=MaterialTheme.colorScheme.secondaryContainer) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(if(e.trending) "↗ Trending right now" else "Finding its people",style=MaterialTheme.typography.titleMedium)
            Text("${e.recent} sample signups in 24h · ${e.signups+if(joined) 1 else 0} total")
            Text("Trending means 20+ signups in 24 hours and at least 50% growth over the previous 24 hours. Counts are sample data.",style=MaterialTheme.typography.bodySmall)
        } }
        Button(shape=androidx.compose.ui.graphics.RectangleShape,onClick=onJoin,modifier=Modifier.fillMaxWidth()) { Text(if(joined) "✓ On the demo list · undo" else "Count me in · demo signup") }
        OutlinedButton(shape=androidx.compose.ui.graphics.RectangleShape,onClick=onSave,modifier=Modifier.fillMaxWidth()) { Text(if(saved) "♥ Saved · remove" else "♡ Save for later") }
        OutlinedButton(shape=androidx.compose.ui.graphics.RectangleShape,onClick={
            context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.openstreetmap.org/?mlat=${e.latitude}&mlon=${e.longitude}#map=15/${e.latitude}/${e.longitude}")))
        },modifier=Modifier.fillMaxWidth()) { Text("Open sample area in Maps ↗") }
        TextButton(shape=androidx.compose.ui.graphics.RectangleShape,onClick={
            val share=Intent(Intent.ACTION_SEND).apply { type="text/plain";putExtra(Intent.EXTRA_TEXT,"${context.getString(R.string.app_name)} demo: ${e.title}\n${e.dateLabel} ${e.time} · ${e.priceLabel}\n${e.venue}\n${e.activities.joinToString()}\nFictional sample event; no actual reservation.") }
            context.startActivity(Intent.createChooser(share,"Share sample event"))
        }) { Text("Share event summary") }
        Text("Demo only. No ticket is issued, no payment is collected, and listings are fictional. Optional purchases may cost extra.",style=MaterialTheme.typography.bodySmall)
    }
}


