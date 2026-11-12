package com.panacea.rougeradar

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Event(
    val id: Int, val title: String, val category: String, val venue: String,
    val daysAway: Long, val time: String, val price: Int, val activities: List<String>,
    val description: String, val signups: Int, val recent: Int, val previous: Int,
    val symbol: String, val miles: Double, val latitude: Double, val longitude: Double,
    val track: EventTrack? = null
) {
    val trending: Boolean get() = recent >= 20 && recent >= previous * 1.5
    val priceLabel: String get() = if (price == 0) "Free" else "$$price"
    val date: LocalDate get() = LocalDate.now(ZoneId.of("America/Chicago")).plusDays(daysAway)
    val dateLabel: String get() = date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
}

// Fictional examples, deliberately separate from UI for a future repository/API.
object SampleEvents {
    val all = listOf(
        Event(1,"Jazz under the stars","Music","Mid City listening garden",2,"7:00 PM",18,listOf("Live jazz trio","Outdoor seating","Food vendors"),"An easy evening of warm brass and live music under the Louisiana sky. Food is purchased separately.",184,62,25,"♫",2.4,30.449,-91.156,EventTrack("Take Five","The Dave Brubeck Quartet","-DHuW1h1wHw",isMoodMatch=true)),
        Event(2,"One more chapter","Books","Garden District reading room",3,"10:00 AM",0,listOf("Book discussion","Coffee & conversation","Book swap"),"Bring your current read. There is no assigned book, and newcomers are welcome.",32,8,6,"▤",3.1,30.429,-91.161),
        Event(3,"A taste of the Red Stick","Food","Downtown market grounds",3,"11:00 AM",0,listOf("Local food stalls","Cooking demos","Acoustic music"),"Meet local makers. Admission is free; food and drinks are purchased separately.",246,84,36,"♨",0.8,30.452,-91.188),
        Event(4,"Clay, play & a little rosé","Arts","Mid City community studio",4,"6:00 PM",35,listOf("Guided pottery","Materials included","Take-home piece"),"A beginner-friendly pottery session. All tools and clay are included. Drinks cost extra.",28,12,9,"◒",4.2,30.450,-91.137),
        Event(5,"Roll for a good time","Gaming","Highland tabletop lounge",6,"5:30 PM",5,listOf("Board games","Beginner tables","Meet new players"),"Strategy, party games and friendly competition. Hosts can teach the rules.",46,18,14,"⚄",5.6,30.397,-91.150),
        Event(6,"Slow mornings on the levee","Outdoors","Downtown riverfront",7,"8:00 AM",0,listOf("Riverside walk","Bird spotting","All paces welcome"),"A gentle walk along the Mississippi. Bring water and comfortable shoes. Weather dependent.",58,15,10,"↟",1.2,30.446,-91.192)
    )
}


