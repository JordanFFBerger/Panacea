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
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    fun toggle(id:Int,signup:Boolean) {
        val set=if(signup) joined else saved
        val updated=if(id.toString() in set) set-id.toString() else set+id.toString()
        if(signup) joined=updated else saved=updated
        prefs.edit().putStringSet(if(signup) "joined" else "saved",updated).apply()
    }
    RadarTheme(mode) {
        BackHandler(selected!=-1) { selected=-1 }
        Scaffold(topBar={ Surface { Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            Text("↗ rouge radar",style=MaterialTheme.typography.titleLarge)
            TextButton(shape=androidx.compose.foundation.shape.RectangleShape,onClick={mode=when(mode){"Auto"->"Day";"Day"->"Night";else->"Auto"};prefs.edit().putString("theme",mode).apply()}) { Text("◐ $mode") }
        } } },bottomBar={ Surface(tonalElevation=3.dp) { Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(4.dp)) {
            listOf("Discover","Nearby","Saved","Profile").forEach { name ->
                TextButton(shape=androidx.compose.foundation.shape.RectangleShape,onClick={tab=name;selected=-1},modifier=Modifier.weight(1f)) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(when(name){"Discover"->"⌂";"Nearby"->"◎";"Saved"->"♡";else->"☷"},style=MaterialTheme.typography.titleLarge)
                        Text(name,style=MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } } }) { padding ->
            Box(Modifier.padding(padding)) {
                val event=SampleEvents.all.find { it.id==selected }
                if(event!=null) EventDetails(event,event.id.toString() in saved,event.id.toString() in joined,{selected=-1},{toggle(event.id,false)},{toggle(event.id,true)})
                else EventDiscovery(tab,saved,joined,{selected=it},{toggle(it,false)})
            }
        }
    }
}

