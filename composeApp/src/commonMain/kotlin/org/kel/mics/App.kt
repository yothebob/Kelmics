package org.kel.mics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.kel.mics.Mal.mal_rep
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kel.mics.Buffers.BufferCore

import org.kel.mics.Mal.mal_rep2
import org.kel.mics.Mal.mal_rep3
import org.kel.mics.Mal.mal_rep4

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var init = BufferCore()
        var input = remember { mutableStateOf("") }
        var output = remember { mutableStateOf("") }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(value = input.value, onValueChange = { input.value = it})
            Button(onClick = { output.value = mal_rep4(input.value) }) {
                Text("Evaluate")
            }
            Text(text=output.value.toString())
        }
    }
}