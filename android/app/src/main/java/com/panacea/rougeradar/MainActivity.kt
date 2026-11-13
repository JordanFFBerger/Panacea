package com.panacea.rougeradar

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import kotlinx.coroutines.CancellationException
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RadarApp() }
    }
}

@Composable
fun RadarApp() {
    val context=LocalContext.current
    val prefs=remember { context.getSharedPreferences("radar",Context.MODE_PRIVATE) }
    var mode by remember { mutableStateOf(prefs.getString("theme","Auto") ?: "Auto") }
    var saved by remember { mutableStateOf(prefs.getStringSet("saved",emptySet())!!.toSet()) }
    var joined by remember { mutableStateOf(prefs.getStringSet("joined",emptySet())!!.toSet()) }
    var tab by rememberSaveable { mutableStateOf("Discover") }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var remoteEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var catalogStatus by remember { mutableStateOf("Loading event catalog…") }
    val events = remoteEvents
    var refresh by remember { mutableIntStateOf(0) }
    var catalogFailed by remember { mutableStateOf(false) }
    val discoveryState = rememberSaveableStateHolder()

    LaunchedEffect(refresh) {
        catalogFailed = false
        catalogStatus = "Loading event catalog…"
        try {
            remoteEvents = EventApiClient.fetchEvents().distinctBy { it.id }.map { it.toDisplayEvent() }
            catalogStatus = if (remoteEvents.isEmpty()) "No upcoming events from our sources"
                            else "Baton Rouge event catalog"
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            catalogFailed = true
            catalogStatus = "Unable to load events. Check your connection and try again."
            android.util.Log.e(
                "PanaceaAPI",
                "Failed to load events",
                error
            )
        }
    }
    fun toggle(id:String,signup:Boolean) {
        val set=if(signup) joined else saved
        val updated=if(id in set) set-id else set+id
        if(signup) joined=updated else saved=updated
        prefs.edit().putStringSet(if(signup) "joined" else "saved",updated).apply()
    }
    RadarTheme(mode) {
        BackHandler(selected!=null) { selected=null }
        Scaffold(topBar={ Surface { Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            Text(androidx.compose.ui.res.stringResource(R.string.app_name),style=MaterialTheme.typography.titleLarge)
            TextButton(shape=androidx.compose.ui.graphics.RectangleShape,onClick={mode=when(mode){"Auto"->"Day";"Day"->"Night";else->"Auto"};prefs.edit().putString("theme",mode).apply()}) { Text("◐ $mode") }
        } } },bottomBar={ Surface(tonalElevation=3.dp) { Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(4.dp)) {
            listOf("Discover","Nearby","Saved","Profile").forEach { name ->
                TextButton(shape=androidx.compose.ui.graphics.RectangleShape,onClick={tab=name;selected=null},modifier=Modifier.weight(1f)) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(when(name){"Discover"->"⌂";"Nearby"->"◎";"Saved"->"♡";else->"☷"},style=MaterialTheme.typography.titleLarge)
                        Text(name,style=MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } } }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (tab == "Discover" && selected == null) DiscoverBackdrop(Modifier.matchParentSize())
                val event=events.find { it.key==selected }
                if(event!=null) EventDetails(event,event.key in saved,event.key in joined,{selected=null},{toggle(event.key,false)},{toggle(event.key,true)})
                else discoveryState.SaveableStateProvider(tab) {
                    EventDiscovery(events,catalogStatus,tab,saved,joined,{selected=it},{toggle(it,false)},if(catalogFailed) ({ refresh++ }) else null)
                }
            }
        }
    }
}
