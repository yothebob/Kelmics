package org.kel.mics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kel.mics.Buffers.BufferCore
import org.kel.mics.Mal.Env
import org.kel.mics.Mal.ISeq
import org.kel.mics.Mal.MalFunction
import org.kel.mics.Mal.MalList
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalSymbol
import org.kel.mics.Mal.eval
import org.kel.mics.Mal.ns
import org.kel.mics.Mal.rep

var history = mutableListOf<String>()

fun mmm () : Env {
    val repl_env = Env()
    ns.forEach({ it -> repl_env.set(it.key, it.value) })

    // repl_env.set(MalSymbol("*ARGV*"), MalList(args.drop(1).map({ it -> MalString(it) }).toMutableList()))
    repl_env.set(MalSymbol("eval"), MalFunction({ a: ISeq -> eval(a.first(), repl_env) }))
    rep("(def! not (fn* (a) (if a false true)))", repl_env)
    rep("(def! load-file (fn* (f) (eval (read-string (str \"(do \" (slurp f) \"\nnil)\")))))", repl_env)
    return repl_env

//if (args.any()) {
//    rep("(load-file \"${args[0]}\")", repl_env)
//    return
//}
}
val repl_env = mmm()

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
                output.value = rep(input.value, repl_env)}) {
                Text("Evaluate")
            }
            Text(text=output.value.toString())
        }
    }
}