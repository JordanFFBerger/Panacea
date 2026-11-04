package com.panacea.rougeradar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Discovery() } }
    }
}

@Composable
fun Discovery() {
    Surface(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.safeDrawingPadding()) {
            item { Text("ROUGE RADAR", style = MaterialTheme.typography.headlineMedium) }
            item { Text("Baton Rouge · Sample events") }
            items(SampleEvents.all, key = { it.id }) { event ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${event.category} · ${event.priceLabel}")
                        Text(event.title, style = MaterialTheme.typography.titleLarge)
                        Text("${event.dateLabel} · ${event.time}")
                        Text(event.venue)
                        Text(event.activities.joinToString(" · "))
                    }
                }
            }
        }
    }
}
