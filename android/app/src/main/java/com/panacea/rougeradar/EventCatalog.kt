package com.panacea.rougeradar

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun RemoteEvent.toDisplayEvent(): Event = Event(
    id = 0, title = title, category = category, venue = venue,
    daysAway = 0,
    time = if (timeType == "date_only") "Time not listed" else
        OffsetDateTime.parse(startLocal).format(DateTimeFormatter.ofPattern("h:mm a", Locale.US)),
    price = 0, activities = emptyList(), description = description.orEmpty(),
    signups = 0, recent = 0, previous = 0, symbol = "✦", miles = null,
    latitude = latitude, longitude = longitude, listing = this
)
