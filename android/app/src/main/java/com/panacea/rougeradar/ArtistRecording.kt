package com.panacea.rougeradar

internal fun artistRecording(eventTitle: String): Pair<String, String>? {
    // Only match known performers at the start of a listing. Tribute shows may name the act.
    val title = eventTitle.trim()
    if (Regex("""\b(tribute|cover band|tribute act)\b""", RegexOption.IGNORE_CASE).containsMatchIn(title)) return null
    return when {
        Regex("""^Tracy Lawrence(?:$|\s+w(?:/|ith\b)|\s*[:–—-])""", RegexOption.IGNORE_CASE).containsMatchIn(title) ->
            "Tracy Lawrence · Time Marches On" to "https://www.youtube.com/watch?v=7UZ4eU58pPo"
        Regex("""^3 Years Hollow(?:$|\s+w(?:/|ith\b)|\s*[:–—-])""", RegexOption.IGNORE_CASE).containsMatchIn(title) ->
            "3 Years Hollow · Remember" to "https://www.imagenrecords.com/discography/3-years-hollow/"
        else -> null
    }
}
