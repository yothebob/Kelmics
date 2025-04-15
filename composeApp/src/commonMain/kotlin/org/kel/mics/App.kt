package org.kel.mics

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import okio.Buffer
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kel.mics.Buffers.KelBuffer
import org.kel.mics.Mal.Env
import org.kel.mics.Mal.ISeq
import org.kel.mics.Mal.MalFunction
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalSymbol
import org.kel.mics.Mal.NIL
import org.kel.mics.Mal.eval
import org.kel.mics.Mal.ns
import org.kel.mics.Mal.rep

var MESSAGES_BUFFER = KelBuffer(name="*Messages*")
var CURRENT_BUFFER = mutableStateOf<KelBuffer>(MESSAGES_BUFFER)
var HISTORY_BUFFER = KelBuffer(name="*History*")
var ASYNC_BUFFER = KelBuffer("*Remote-msg*")
var BUFFERS = mutableStateListOf<KelBuffer>(MESSAGES_BUFFER, ASYNC_BUFFER, HISTORY_BUFFER)



fun mmm () : Env {
    val repl_env = Env()
    ns.forEach({ it -> repl_env.set(it.key, it.value) })

    // repl_env.set(MalSymbol("*ARGV*"), MalList(args.drop(1).map({ it -> MalString(it) }).toMutableList()))

    repl_env.set(MalSymbol("create-buffer"), MalFunction({ a: ISeq ->
        // TODO:
        val nameCount = BUFFERS.count { it.name.replace(Regex("/<\\d+>$"), "") == a.first().getVal().toString() }
        val buffName = if (nameCount == 0) a.first().getVal().toString() else "${a.first().getVal().toString()}<${nameCount}>"
        val newBuffer = KelBuffer(name=buffName)
        BUFFERS.add(newBuffer)
        if (a.nth(1) != null) {
            newBuffer.buf.writeUtf8((a.nth(1) as MalString).getVal())
        }
        NIL
    })) // (create-buffer "name")
    repl_env.set(MalSymbol("switch-buffer"), MalFunction({ a: ISeq ->
        // TODO: have a wrapper class around okio buffer for holding name and stuff.
        val foundBuffer = BUFFERS.first { it.name == a.first().getVal() }
        if (foundBuffer != null) {
            CURRENT_BUFFER.value = foundBuffer
        }
        NIL
    })) //(switch-buffer "BUFFER_NAME")


    repl_env.set(MalSymbol("eval"), MalFunction({ a: ISeq -> eval(a.first(), repl_env) }))
    repl_env.set(MalSymbol("ns"), MalFunction({ a: ISeq -> repl_env.showNamespace() }))
    repl_env.set(MalSymbol("show-variable"), MalFunction({ a: ISeq ->
        println(repl_env.get("test")?.mal_print())
        NIL
    }))



    rep("(def! not (fn* (a) (if a false true)))", repl_env)
    rep("(def! find-file (fn* (fname) (create-buffer fname (slurp fname))))", repl_env)
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
    buffer: Buffer = Buffer(),
    contents: MutableState<String>,
    name: String = ""
    ) {
    Column (modifier=Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().border(2.dp, color = Color.Blue)) { // TODO: replace this with a modebar?
            Text(text = "Buffer: ${name}\n", fontStyle = FontStyle.Italic, modifier = Modifier)
        }
        Box {
            if (readOnly) {
                Text(text = contents.value)
            } else {
                // TODO: Textarea
                Text(text = "Buffer: ${name}\n", fontStyle = FontStyle.Italic, modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
fun MiniBuffer(modifier: Modifier = Modifier, bufferContents: MutableState<String>) {
    var input = remember { mutableStateOf("") }
    var output = remember { mutableStateOf("") }

    Row(modifier = modifier) {
        TextField(value = input.value,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onValueChange = { input.value = it}
        )
    }
    Row {
        Button(onClick = {
            output.value = rep(input.value, repl_env)
            HISTORY_BUFFER.buf.writeUtf8("${input.value}\n")
            MESSAGES_BUFFER.buf.writeUtf8("${output.value}\n")
            bufferContents.value = MESSAGES_BUFFER.buf.snapshot().utf8()
        }) {
            Text("Evaluate")
        }
    }
}

@Composable
@Preview
fun App() {
    val contents = remember { mutableStateOf("") }
    MaterialTheme {
        Column {
            MultiBufferExample()
//            MiniBuffer(bufferContents=contents)
//            Row(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
//                MalBuffer(
//                    modifier = Modifier.padding(10.dp),
//                    readOnly = true,
//                    buffer = CURRENT_BUFFER.value,
//                    contents = contents,
//                    name = "*Messages*"
//                )
//            }
        }
    }
}


@Composable
fun MultiBufferExample() {
    var text by remember { mutableStateOf("") }
    var displayText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BUFFERS.forEach { buff ->
                Button(
                    onClick = {
                        CURRENT_BUFFER.value = buff
                        displayText = buff.buf.snapshot().utf8()
                    },
                    colors = if (buff.name == CURRENT_BUFFER.value.name) ButtonDefaults.buttonColors(MaterialTheme.colors.primary)
                    else ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                ) {
                    Text(buff.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = text,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onValueChange = { text = it },
            label = { Text("") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            HISTORY_BUFFER.buf.writeUtf8("${text}\n")
            MESSAGES_BUFFER.buf.writeUtf8("${rep(text, repl_env)}\n")
            displayText = CURRENT_BUFFER.value.buf.snapshot().utf8()
            text = ""
        }) {
            Text("Eval")
        }

        Spacer(modifier = Modifier.height(16.dp))
        SelectionContainer {
            LazyColumn(modifier = Modifier.fillMaxHeight(1f)) {
                items(displayText.lines().size) { i ->
                        Text(displayText.lines()[i])
                    }
            }
        }
    }
}
