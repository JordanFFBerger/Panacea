package com.panacea.rougeradar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.ZonedDateTime
import java.time.ZoneId

private val Day = lightColorScheme(primary=Color(0xFF176654),onPrimary=Color.White,background=Color(0xFFF3F5EF),onBackground=Color(0xFF173A3A),surface=Color(0xFFFFFEFA),onSurface=Color(0xFF173A3A),secondaryContainer=Color(0xFFD6EDE3),onSecondaryContainer=Color(0xFF173A3A),outline=Color(0xFF729487))
private val Night = darkColorScheme(primary=Color(0xFFE2BF79),onPrimary=Color(0xFF152632),background=Color(0xFF101E29),onBackground=Color(0xFFF0E8CF),surface=Color(0xFF182A35),onSurface=Color(0xFFF0E8CF),secondaryContainer=Color(0xFF304551),onSecondaryContainer=Color(0xFFF0E8CF),outline=Color(0xFF849A9D))

@Composable
fun RadarTheme(mode: String, content: @Composable () -> Unit) {
    var hour by remember { mutableIntStateOf(12) }
    LaunchedEffect(Unit) {
        while (true) {
            hour = ZonedDateTime.now(ZoneId.of("America/Chicago")).hour
            delay(60_000)
        }
    }
    val dark = mode == "Night" || mode == "Auto" && (hour < 7 || hour >= 18)
    MaterialTheme(colorScheme=if(dark) Night else Day,
        shapes=Shapes(extraSmall=androidx.compose.foundation.shape.RoundedCornerShape(0.dp),small=androidx.compose.foundation.shape.RoundedCornerShape(0.dp),medium=androidx.compose.foundation.shape.RoundedCornerShape(0.dp),large=androidx.compose.foundation.shape.RoundedCornerShape(0.dp),extraLarge=androidx.compose.foundation.shape.RoundedCornerShape(0.dp)),
        content=content)
}

@Composable
fun RiverHero(onPick: (() -> Unit)?) {
    val dark = MaterialTheme.colorScheme.background == Night.background
    val ink = MaterialTheme.colorScheme.onBackground
    val colors = if (dark) listOf(Color(0xFF14283A), Color(0xFF45324E))
                 else listOf(Color(0xFFF1FFE2), Color(0xFFA1E2EE), Color(0xFFDAF1C2))
    Column(
        Modifier.fillMaxWidth().background(Brush.linearGradient(colors)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (dark) "✦  AFTER HOURS" else "✦  BATON ROUGE, LA",
                    color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium
                )
                Text(
                    stringResource(R.string.app_tagline), color = ink,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = if (dark) FontFamily.Serif else FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text("Meet Panacea, your goddess of things to do.", color = ink, style = MaterialTheme.typography.bodySmall)
            }
            Surface(
                modifier = Modifier.weight(.65f), color = Color(0xFFFCF2D9),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Image(
                    painterResource(R.drawable.goddess_panacea),
                    contentDescription = "Panacea in draped robes, Archaeological Museum of Dion",
                    modifier = Modifier.fillMaxWidth().height(148.dp).padding(5.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Button(
            onClick = { onPick?.invoke() }, enabled = onPick != null,
            shape = RectangleShape, modifier = Modifier.fillMaxWidth()
        ) { Text("✦  Pick a cure for me") }
    }
}
