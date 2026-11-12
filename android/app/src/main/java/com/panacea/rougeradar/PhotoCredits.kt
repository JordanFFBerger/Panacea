package com.panacea.rougeradar

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private data class PhotoCredit(val title: String, val author: String, val source: String, val license: String, val terms: String)
private val photoCredits = listOf(
    PhotoCredit("Ravi Coltrane at the Blue Note", "Frank Schulenburg", "https://commons.wikimedia.org/wiki/File:Ravi_Coltrane_at_the_Blue_Note%2C_March_7%2C_2023-L1002389.jpg", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/"),
    PhotoCredit("Warm cup of coffee on an open book", "Shixart1985", "https://commons.wikimedia.org/wiki/File:Warm_cup_of_coffee_sits_on_an_open_book_on_a_wooden_table_in_a_cozy_workspace.jpg", "CC BY 2.0", "https://creativecommons.org/licenses/by/2.0/"),
    PhotoCredit("Tomatoes at Hope Farmers Market", "Larry D. Moore", "https://commons.wikimedia.org/wiki/File:Tomatoes_at_Hope_Farmers_Market.jpg", "CC BY 4.0", "https://creativecommons.org/licenses/by/4.0/"),
    PhotoCredit("Hands shaping clay on a pottery wheel", "Tuan Nguyen", "https://unsplash.com/photos/hands-shaping-clay-on-a-pottery-wheel-_fxvuMucGDs", "Unsplash License", "https://unsplash.com/license"),
    PhotoCredit("Exploration (game) Dice", "Smartiperson", "https://commons.wikimedia.org/wiki/File:Exploration_(game)_Dice.jpg", "CC0 1.0", "https://creativecommons.org/publicdomain/zero/1.0/"),
    PhotoCredit("Baton Rouge 2017 By the Levee", "SnippyHolloW", "https://commons.wikimedia.org/wiki/File:Baton_Rouge_2017_By_the_Levee.jpg", "CC BY-SA 2.0", "https://creativecommons.org/licenses/by-sa/2.0/"),
    PhotoCredit("Panacea Helping the Sick", "J. Gazola (1716)", "https://commons.wikimedia.org/wiki/File:Panacea.jpg", "Public domain", "https://creativecommons.org/publicdomain/mark/1.0/")
)

@Composable
fun PhotoCredits() {
    val context = LocalContext.current
    var show by remember { mutableStateOf(false) }
    var noHandler by remember { mutableStateOf(false) }
    fun open(url: String) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        catch (_: ActivityNotFoundException) { noHandler = true }
    }
    TextButton(onClick = { show = true; noHandler = false }, shape = RectangleShape) { Text("Photo credits") }
    if (show) AlertDialog(
        onDismissRequest = { show = false },
        title = { Text("Photo credits") },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                Text("Event photos are illustrative and may be cropped in the app.")
                if (noHandler) Text("No browser is available to open links.")
                photoCredits.forEach { credit ->
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text(credit.title, style = MaterialTheme.typography.titleSmall)
                    Text(credit.author, style = MaterialTheme.typography.bodySmall)
                    Row {
                        TextButton(onClick = { open(credit.source) }, shape = RectangleShape) { Text("Source ↗") }
                        TextButton(onClick = { open(credit.terms) }, shape = RectangleShape) { Text(credit.license) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { show = false }, shape = RectangleShape) { Text("Done") } }
    )
}
