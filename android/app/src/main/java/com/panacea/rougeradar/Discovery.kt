package com.panacea.rougeradar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EventDiscovery(events:List<Event>,catalogStatus:String,tab:String,saved:Set<String>,joined:Set<String>,onOpen:(String)->Unit,onSave:(String)->Unit,onRetry:(()->Unit)? = null) {
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    var period by rememberSaveable { mutableStateOf("Any date") }
    var price by rememberSaveable { mutableStateOf("Any price") }
    var sort by rememberSaveable { mutableStateOf(if (tab == "Nearby") "Distance" else "Soonest") }
    val today = java.time.LocalDate.now(java.time.ZoneId.of("America/Chicago"))
    val filtered=events.filter { e ->
        (tab != "Saved" || e.key in saved) &&
        (category=="All" || e.category==category) &&
        (query.isBlank() || "${e.title} ${e.venue} ${e.category} ${e.activities.joinToString()}".contains(query,true)) &&
        (price!="Free" || e.amount==0.0) && (price!="Under $25" || (e.amount?.let { it < 25.0 } == true)) &&
        (period!="This week" || e.date in today..today.plusDays(7)) && (period!="Weekend" || e.date.dayOfWeek.value>=6)
    }.let { list -> when (sort) {
        "Distance" -> list.sortedBy { it.miles ?: Double.POSITIVE_INFINITY }
        "Trending" -> list.sortedByDescending { it.recent }
        "Price" -> list.sortedBy { it.amount ?: Double.POSITIVE_INFINITY }
        else -> list.sortedBy { it.date }
    } }
    LazyColumn(modifier=Modifier.testTag("event-list"),contentPadding=PaddingValues(start=16.dp,end=16.dp,bottom=16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
        if(tab=="Discover") item { RiverHero(onPick = if (filtered.isEmpty()) null else ({ onOpen(filtered.random().key) })) }
        item { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text(when(tab){"Nearby"->"Good things, close by";"Saved"->"Saved Events";else->"Around the Red Stick"},modifier=if(tab=="Saved") Modifier.fillMaxWidth() else Modifier,textAlign=if(tab=="Saved") androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start,style=MaterialTheme.typography.headlineSmall)
            Text(if(tab=="Saved") "Local guest · events saved on this device" else catalogStatus,style=MaterialTheme.typography.labelMedium)
            onRetry?.let { OutlinedButton(shape=RectangleShape,onClick=it) { Text("Retry event feed") } }
            if(tab=="Nearby" && sort=="Distance") Text("Known distances first. Check the event for venue information.",style=MaterialTheme.typography.bodySmall)
        } }
        item { OutlinedTextField(value=query,onValueChange={query=it},label={Text("Search events, activities, places")},singleLine=true,modifier=Modifier.fillMaxWidth().testTag("event-search"),shape=RectangleShape,
            keyboardOptions=KeyboardOptions(imeAction=ImeAction.Search),
            keyboardActions=KeyboardActions(onSearch={focusManager.clearFocus()})) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            (listOf("All","Music","Books","Food","Arts","Gaming","Outdoors","Sports","Film","Comedy","Nightclubs") + events.map { it.category }).distinct().forEach { name -> FilterChip(selected=category==name,onClick={category=name},label={Text(name)},shape=RectangleShape) }
        } }
        item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            FilterMenu(period,listOf("Any date","This week","Weekend"),Modifier.weight(1f)){period=it}
            FilterMenu(price,listOf("Any price","Free","Under $25"),Modifier.weight(1f)){price=it}
            FilterMenu(sort, if (tab == "Nearby") listOf("Distance","Soonest","Price") else listOf("Soonest","Price"),Modifier.weight(1f)){sort=it}
        } }
        if (tab == "Saved") item { PhotoCredits() }
        item { Text("${filtered.size} events",style=MaterialTheme.typography.labelSmall) }
        if(filtered.isEmpty()) item { Column(Modifier.padding(vertical=24.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("A little quiet here.",style=MaterialTheme.typography.titleLarge)
            Text(if(tab=="Saved") "Save an event from Discover to find it here." else "Try another search or filter.")
            OutlinedButton(shape=androidx.compose.ui.graphics.RectangleShape,onClick={query="";category="All";price="Any price";period="Any date"}) { Text("Reset filters") }
        } }
        items(filtered,key={it.key}) { e -> EventCard(e,e.key in saved,e.key in joined,tab=="Nearby",{onOpen(e.key)},{onSave(e.key)}) }
        if (events.any { it.listing?.sourceName == "Has Things" }) item {
            val context = androidx.compose.ui.platform.LocalContext.current
            TextButton(shape=RectangleShape,onClick={ context.openEventUrl("https://hasthings.com") }) {
                Text("Data: Has Things (hasthings.com)")
            }
        }
    }
}

@Composable
fun FilterMenu(value:String,options:List<String>,modifier:Modifier=Modifier,onChange:(String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(shape=androidx.compose.ui.graphics.RectangleShape,onClick={expanded=true},modifier=Modifier.fillMaxWidth(),contentPadding=PaddingValues(horizontal=6.dp)) { Text("$value ▾",style=MaterialTheme.typography.labelSmall) }
        DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) { options.forEach { option -> DropdownMenuItem(text={Text(option)},onClick={onChange(option);expanded=false}) } }
    }
}

@Composable
fun EventCard(e:Event,saved:Boolean,joined:Boolean,nearby:Boolean,onOpen:()->Unit,onSave:()->Unit) {
    OutlinedCard(modifier=Modifier.fillMaxWidth().clickable(onClick=onOpen),shape=RectangleShape) {
        EventCover(e, Modifier.fillMaxWidth().height(132.dp))
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(start=18.dp,end=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            Text(if(e.track != null) "♫  In-app listening room" else if(e.isSample) "✦  Demo listing" else e.listing?.sourceName ?: "✦  Catalog event",style=MaterialTheme.typography.labelMedium)
            TextButton(shape=androidx.compose.ui.graphics.RectangleShape,onClick=onSave) { Text(if(saved) "♥ Unsave" else "♡ Save") }
        }
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text(e.dateLabel,style=MaterialTheme.typography.labelMedium)
                Text(e.priceLabel,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)
            }
            Text(e.title,style=MaterialTheme.typography.titleLarge)
            Text("${e.time} · ${e.venue}",style=MaterialTheme.typography.bodySmall)
            if(e.activities.isNotEmpty()) Text(e.activities.joinToString(" · "),style=MaterialTheme.typography.bodyMedium)
            if(e.isSample) HorizontalDivider()
            if(e.isSample) Text("${if(e.trending) "↗ Trending" else "Gathering good company"} · ${e.signups+if(joined) 1 else 0} signed up",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
            if(nearby && e.miles != null) Text("${e.miles} mi · Sample distance",style=MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun AttendanceProfile(events: List<Event>, saved: Set<String>, onOpen: (String) -> Unit, onSave: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("event-list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        item {
            Text("Past Events", Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall)
        }
        if (events.isEmpty()) item {
            Text("Mark an event “I went!” to add it to your profile.", style = MaterialTheme.typography.bodyMedium)
        }
        items(events.sortedByDescending { it.listing?.startLocal }, key = { it.key }) { event ->
            EventCard(event, event.key in saved, false, false, { onOpen(event.key) }, { onSave(event.key) })
        }
        if (events.any { it.listing?.sourceName == "Has Things" }) item {
            Text("Data: Has Things (hasthings.com)", style = MaterialTheme.typography.bodySmall)
        }
    }
}
