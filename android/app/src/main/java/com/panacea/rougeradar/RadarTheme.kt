package com.panacea.rougeradar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
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
fun DiscoverBackdrop(modifier: Modifier = Modifier) {
    val dark = MaterialTheme.colorScheme.background == Night.background
    Image(
        painterResource(if (dark) R.drawable.discover_night else R.drawable.discover_day),
        contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop,
        alpha = if (dark) .18f else .12f
    )
}

@Composable
fun RiverHero(onPick: (() -> Unit)?) {
    val dark = MaterialTheme.colorScheme.background == Night.background
    val ink = MaterialTheme.colorScheme.onBackground
    val uriHandler = LocalUriHandler.current
    var showPanacea by rememberSaveable { mutableStateOf(false) }
    val colors = if (dark) listOf(Color(0xF514233A), Color(0xBA192B43), Color(0x25331C45))
                 else listOf(Color(0xF7E7FFEB), Color(0xC4C0E9F3), Color(0x3894DDD6))
    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer)) {
        Image(
            painterResource(if (dark) R.drawable.discover_night else R.drawable.discover_day),
            contentDescription = null, modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop
        )
        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(colors)))
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (dark) "✦  AFTER HOURS" else "✦  BATON ROUGE, LA",
                        color = ink, style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        stringResource(R.string.app_tagline), color = ink,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = if (dark) FontFamily.Serif else FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    TextButton(
                        onClick = { showPanacea = true }, shape = RectangleShape,
                        colors = ButtonDefaults.textButtonColors(contentColor = ink),
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) { Text("Meet Panacea", textDecoration = TextDecoration.Underline) }
                }
                Surface(
                    modifier = Modifier.weight(.58f), color = Color(0xFFFCF2D9),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Image(
                        painterResource(R.drawable.goddess_panacea),
                        contentDescription = "Panacea in draped robes, Archaeological Museum of Dion",
                        modifier = Modifier.fillMaxWidth().height(116.dp).padding(3.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Button(
                onClick = { onPick?.invoke() }, enabled = onPick != null,
                shape = RectangleShape, modifier = Modifier.fillMaxWidth()
            ) { Text("✦  Pick a cure for me") }
        }
    }
    if (showPanacea) AlertDialog(
        onDismissRequest = { showPanacea = false },
        title = { Text("Panacea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.panacea_source_excerpt))
                Text(stringResource(R.string.panacea_boredom_note))
                TextButton(
                    onClick = { uriHandler.openUri("https://www.theoi.com/Ouranios/AsklepiasPanakeia.html") },
                    shape = RectangleShape, contentPadding = PaddingValues(horizontal = 0.dp)
                ) { Text("Theoi Project ↗") }
            }
        },
        confirmButton = { TextButton(onClick = { showPanacea = false }, shape = RectangleShape) { Text("Done") } }
    )
}
