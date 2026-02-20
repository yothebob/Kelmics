package org.kel.mics.Buffers.MajorModes

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp



@Composable
expect fun WebView(loaded: MutableState<Boolean>, failed: MutableState<Boolean>, url: String)


@Composable
fun WebBuffer(url: String = "https://google.com") {
    val loaded = remember { mutableStateOf(false) }
    val failed = remember { mutableStateOf(false) }
    Column {
        WebView(loaded, failed, url)
    }
    if (!loaded.value) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(100.dp))
            CircularProgressIndicator(modifier = Modifier.padding(10.dp),
                color = MaterialTheme.colors.primary)
        }
    }
    if (failed.value) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text="Browser failed to load...")
        }
    }
}

@Composable
fun WebMode() {
    // TODO: pop up a mini buffer asking for url, then route to url.. on a keyclick do popup getting url...
    var url = remember { mutableStateOf("https://google.com")}
//    val requester = remember { FocusRequester() }

    WebBuffer()

//    Box(Modifier        .onKeyEvent {
//        if (it.isCtrlPressed && it.key == Key.A) {
//            println("Ctrl + A is pressed")
//            true
//        } else {
//            // let other handlers receive this event
//            false
//        }
//    }.focusRequester(requester)
//        .focusable()
//        .size(10.dp)) {
//    }

//    LaunchedEffect(Unit) {
//        requester.requestFocus()
//    }
}

