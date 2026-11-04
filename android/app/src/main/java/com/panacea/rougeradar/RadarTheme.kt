package com.panacea.rougeradar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
        shapes=Shapes(extraSmall=RectangleShape,small=RectangleShape,medium=RectangleShape,large=RectangleShape,extraLarge=RectangleShape),
        content=content)
}

@Composable
fun RiverHero() {
    val dark = MaterialTheme.colorScheme.background == Night.background
    val ink = MaterialTheme.colorScheme.onBackground
    Box(Modifier.fillMaxWidth().height(218.dp).background(Brush.linearGradient(if(dark) listOf(Color(0xFF172C40),Color(0xFF395565)) else listOf(Color(0xFFD3F3E0),Color(0xFF8AD8DF))))) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(brush=Brush.radialGradient(listOf(Color(0xFFFFF5CD),Color(0xFFC5DDBB)),center=Offset(size.width*.82f,68.dp.toPx()),radius=50.dp.toPx()),radius=50.dp.toPx(),center=Offset(size.width*.82f,68.dp.toPx()))
            drawOval(Color(0xFF70BFCB).copy(alpha=.5f),topLeft=Offset(size.width*.20f,size.height*.62f),size=Size(size.width*1.2f,size.height*.8f))
            drawOval(Color.White.copy(alpha=.35f),topLeft=Offset(size.width*.25f,size.height*.75f),size=Size(size.width*1.3f,size.height*.6f))
            val left=size.width*.57f
            listOf(36f,50f,85f,43f,26f).forEachIndexed { i,h ->
                drawRect(Color(0xFF3C7977),topLeft=Offset(left+i*18.dp.toPx(),size.height-26.dp.toPx()-h.dp.toPx()),size=Size(13.dp.toPx(),h.dp.toPx()))
            }
            if(dark) listOf(.12f,.35f,.55f,.95f).forEach { x -> drawCircle(Color(0xFFE2BF79),2.dp.toPx(),Offset(size.width*x,20.dp.toPx())) }
        }
        Column(Modifier.padding(22.dp).fillMaxWidth(.72f), verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("BATON ROUGE, LOUISIANA",style=MaterialTheme.typography.labelSmall,color=ink)
            Text(if(dark) "A good night\nstarts here." else "A little out there.\nA lot to get into.",style=MaterialTheme.typography.headlineMedium.copy(fontFamily=if(dark) FontFamily.Serif else FontFamily.SansSerif),color=ink)
            Text("Find a show. Meet your people.",style=MaterialTheme.typography.bodySmall,color=ink)
        }
    }
}

