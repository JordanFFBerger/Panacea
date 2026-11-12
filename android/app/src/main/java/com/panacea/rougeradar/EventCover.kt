package com.panacea.rougeradar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun EventCover(event: Event, modifier: Modifier = Modifier) {
    val photo = when (event.category) {
        "Music" -> R.drawable.cover_music
        "Books" -> R.drawable.cover_books
        "Food" -> R.drawable.cover_food
        "Arts" -> R.drawable.cover_arts
        "Gaming" -> R.drawable.cover_gaming
        "Outdoors" -> R.drawable.cover_outdoors
        else -> null
    }
    Box(modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
        if (photo != null) {
            Image(painterResource(photo), contentDescription = null,
                modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xB8001820)), startY = 35f)
            ))
        }
        Row(
            Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${event.symbol}  ${event.category.uppercase()}",
                Modifier.background(Color(0xFF132E35)).padding(horizontal = 9.dp, vertical = 6.dp),
                color = Color.White, style = MaterialTheme.typography.labelMedium)
            if (event.trending) Text("↗  TRENDING",
                Modifier.background(Color(0xFFFFDA91)).padding(horizontal = 9.dp, vertical = 6.dp),
                color = Color(0xFF342414), style = MaterialTheme.typography.labelSmall)
        }
    }
}
