package org.kel.mics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import org.kel.mics.Mal.mal_rep
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kel.mics.Buffers.BufferCore
import org.kel.mics.Mal.Step5MALREPL

import org.kel.mics.Mal.mal_rep2
import org.kel.mics.Mal.mal_rep3
import org.kel.mics.Mal.mal_rep4
import org.kel.mics.Mal.step5createEnv

var malRepl = Step5MALREPL()
var history = mutableListOf<String>()

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var init = BufferCore()
        var input = remember { mutableStateOf("") }
        var historyItem = remember { mutableStateOf(0) }
        var output = remember { mutableStateOf("") }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(value = input.value, onValueChange = { input.value = it}, modifier = Modifier.onKeyEvent {
                if (it.type == KeyEventType.KeyDown) {
                    when (it.key) {
                        Key.DirectionUp -> {
                            println("up pressed")
                            if (history.size > historyItem.value) {
                                input.value = history.get(history.size - historyItem.value)
                                historyItem.value ++
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            println("down pressed")
                            if (history.size < historyItem.value) {
                                input.value = history.get(history.size + historyItem.value)
                                historyItem.value --
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            })
            Button(onClick = {
                history.add(output.value)
                output.value = malRepl._rep(input.value)}) {
                Text("Evaluate")
            }
            Text(text=output.value.toString())
        }
    }
}