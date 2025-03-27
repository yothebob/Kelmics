package org.kel.mics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kel.mics.Buffers.Buffer
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
var messages = mutableStateOf<String>("")

fun mmm () : Env {
    val repl_env = Env()
    ns.forEach({ it -> repl_env.set(it.key, it.value) })

    // repl_env.set(MalSymbol("*ARGV*"), MalList(args.drop(1).map({ it -> MalString(it) }).toMutableList()))
    repl_env.set(MalSymbol("eval"), MalFunction({ a: ISeq -> eval(a.first(), repl_env) }))
    rep("(def! not (fn* (a) (if a false true)))", repl_env)
    rep("(def! load-file (fn* (f) (eval (read-string (str \"(do \" (slurp f) \"\nnil)\")))))", repl_env)
    rep("(defmacro! cond (fn* (& xs) (if (> (count xs) 0) (list 'if (first xs) (if (> (count xs) 1) (nth xs 1) (throw \"odd number of forms to cond\")) (cons 'cond (rest (rest xs)))))))", repl_env)
    return repl_env

//if (args.any()) {
//    rep("(load-file \"${args[0]}\")", repl_env)
//    return
//}
}
val repl_env = mmm()


@Composable
fun MalBuffer(
    modifier: Modifier = Modifier,
    readOnly: Boolean = true,
    contents: String = "",
    name: String = ""
    ) {
    if (readOnly) {
        Text(text = contents)
    }
    Text(text = "Buffer: ${name}")
}

@Composable
fun MiniBuffer(modifier: Modifier = Modifier) {
    var input = remember { mutableStateOf("") }
    var output = remember { mutableStateOf("") }
    Row(modifier = modifier) {
        TextField(value = input.value,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onValueChange = { input.value = it}
        )
        Button(onClick = {
            output.value = rep(input.value, repl_env)
            println(output.value)
            history.add(output.value)
            messages.value += "${output.value}\n"
        }) {
            Text("Evaluate")
        }
    }
}


@Composable
@Preview
fun App() {
    MaterialTheme {
        Column {
            MiniBuffer()
            MalBuffer(
                modifier = Modifier,
                readOnly = true,
                contents = messages.value,
                name = "*Messages*"
                )
        }
    }
}