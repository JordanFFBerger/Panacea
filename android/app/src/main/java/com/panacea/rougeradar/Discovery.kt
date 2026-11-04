package com.panacea.rougeradar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EventDiscovery(tab:String,saved:Set<String>,joined:Set<String>,onOpen:(Int)->Unit,onSave:(Int)->Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    var period by rememberSaveable { mutableStateOf("Any date") }
    var price by rememberSaveable { mutableStateOf("Any price") }
    var sort by rememberSaveable { mutableStateOf("Soonest") }
    val filtered=SampleEvents.all.filter { e ->
        (tab!="Saved" || e.id.toString() in saved) && (tab!="Profile" || e.id.toString() in joined) &&
        (category=="All" || e.category==category) &&
        (query.isBlank() || "${e.title} ${e.venue} ${e.category} ${e.activities.joinToString()}".contains(query,true)) &&
        (price!="Free" || e.price==0) && (price!="Under $25" || e.price<=25) &&
        (period!="This week" || e.daysAway<=7) && (period!="Weekend" || e.date.dayOfWeek.value>=6)
    }.let { list -> when { tab=="Nearby"->list.sortedBy { it.miles };sort=="Trending"->list.sortedByDescending { it.recent };sort=="Price"->list.sortedBy { it.price };else->list.sortedBy { it.daysAway } } }
    LazyColumn(contentPadding=PaddingValues(start=16.dp,end=16.dp,bottom=20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        if(tab=="Discover") item { RiverHero() }
        item { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text(when(tab){"Nearby"->"Good things, close by";"Saved"->"Saved for a good day";"Profile"->"Your next good stories";else->"Around the Red Stick"},style=MaterialTheme.typography.headlineSmall)
            Text(if(tab=="Profile") "Local guest · plans saved on this device" else "Baton Rouge · sample listings",style=MaterialTheme.typography.labelMedium)
            if(tab=="Nearby") Text("Sorted by sample distance. Open any event for its map link.",style=MaterialTheme.typography.bodySmall)
        } }
        item { OutlinedTextField(value=query,onValueChange={query=it},label={Text("Search events, activities, places")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RectangleShape) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("All","Music","Books","Food","Arts","Gaming","Outdoors").forEach { name -> FilterChip(selected=category==name,onClick={category=name},label={Text(name)},shape=RectangleShape) }
        } }
        item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            FilterMenu(period,listOf("Any date","This week","Weekend"),Modifier.weight(1f)){period=it}
            FilterMenu(price,listOf("Any price","Free","Under $25"),Modifier.weight(1f)){price=it}
            FilterMenu(sort,listOf("Soonest","Trending","Price"),Modifier.weight(1f)){sort=it}
        } }
        item { Text("${filtered.size} events · Demo data",style=MaterialTheme.typography.labelSmall) }
        if(filtered.isEmpty()) item { Column(Modifier.padding(vertical=24.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("A little quiet here.",style=MaterialTheme.typography.titleLarge)
            Text(if(tab=="Saved") "Save an event from Discover to find it here." else if(tab=="Profile") "Demo signups appear here. No real reservation is made." else "Try another search or filter.")
            OutlinedButton(onClick={query="";category="All";price="Any price";period="Any date"}) { Text("Reset filters") }
        } }
        items(filtered,key={it.id}) { e -> EventCard(e,e.id.toString() in saved,e.id.toString() in joined,tab=="Nearby",{onOpen(e.id)},{onSave(e.id)}) }
        item { Text("Prototype only · fictional events, prices and signup counts. No tickets or payments.",style=MaterialTheme.typography.bodySmall) }
    }
}

@Composable
fun FilterMenu(value:String,options:List<String>,modifier:Modifier=Modifier,onChange:(String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth(),contentPadding=PaddingValues(horizontal=6.dp)) { Text("$value ▾",style=MaterialTheme.typography.labelSmall) }
        DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) { options.forEach { option -> DropdownMenuItem(text={Text(option)},onClick={onChange(option);expanded=false}) } }
    }
}

@Composable
fun EventCard(e:Event,saved:Boolean,joined:Boolean,nearby:Boolean,onOpen:()->Unit,onSave:()->Unit) {
    OutlinedCard(modifier=Modifier.fillMaxWidth().clickable(onClick=onOpen),shape=RectangleShape) {
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(start=18.dp,end=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            Text("${e.symbol}  ${e.category.uppercase()}",style=MaterialTheme.typography.labelMedium)
            TextButton(onClick=onSave) { Text(if(saved) "♥ Unsave" else "♡ Save") }
        }
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text(e.dateLabel,style=MaterialTheme.typography.labelMedium)
                Text(e.priceLabel,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)
            }
            Text(e.title,style=MaterialTheme.typography.titleLarge)
            Text("${e.time} · ${e.venue}",style=MaterialTheme.typography.bodySmall)
            Text(e.activities.joinToString(" · "),style=MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            Text("${if(e.trending) "↗ Trending" else "Gathering good company"} · ${e.signups+if(joined) 1 else 0} signed up",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
            if(nearby) Text("${e.miles} mi · Sample distance",style=MaterialTheme.typography.labelSmall)
        }
    }
}
