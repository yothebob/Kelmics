package org.kel.mics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kelmics.composeapp.generated.resources.Res
import okio.Buffer
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kel.mics.Buffers.KelBuffer
import org.kel.mics.Buffers.MajorModes.OrgDocument
import org.kel.mics.Buffers.MajorModes.OrgMode
import org.kel.mics.Buffers.MajorModes.WebMode
import org.kel.mics.Buffers.MajorModes.parseOrg
import org.kel.mics.Mal.Env
import org.kel.mics.Mal.ISeq
import org.kel.mics.Mal.MalFunction
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalSymbol
import org.kel.mics.Mal.NIL
import org.kel.mics.Mal.Namespaces.bufferNs
import org.kel.mics.Mal.Namespaces.ns
import org.kel.mics.Mal.eval
import org.kel.mics.Mal.rep
import kotlin.reflect.typeOf

var MESSAGES_BUFFER = KelBuffer(name="*Messages*")
var CURRENT_BUFFER = mutableStateOf<KelBuffer>(MESSAGES_BUFFER)
var HISTORY_BUFFER = KelBuffer(name="*History*")
var ASYNC_BUFFER = KelBuffer("*Remote-msg*")
var BUFFERS = mutableStateListOf<KelBuffer>(MESSAGES_BUFFER, ASYNC_BUFFER, HISTORY_BUFFER, KelBuffer("*Help*"))
var backgroundColor = mutableStateOf(0XFF15467B)
var textHook = mutableStateOf("")


fun mmm () : Env {
    val repl_env = Env()
    ns.forEach({ it -> repl_env.set(it.key, it.value) })
    bufferNs.forEach({ it -> repl_env.set(it.key, it.value) })

    // repl_env.set(MalSymbol("*ARGV*"), MalList(args.drop(1).map({ it -> MalString(it) }).toMutableList()))

    repl_env.set(MalSymbol("create-buffer"), MalFunction({ a: ISeq ->
        // TODO:
        val nameCount = BUFFERS.count { it.name.replace(Regex("/<\\d+>$"), "") == a.first().getVal().toString() }
        val buffName = if (nameCount == 0) a.first().getVal().toString() else "${a.first().getVal().toString()}<${nameCount}>"
        val newBuffer = KelBuffer(name=buffName)
        BUFFERS.add(newBuffer)
//        if (a.nth(1) != null) {
//            newBuffer.buf.writeUtf8((a.nth(1) as MalString).getVal())
//        }
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
    rep("(def! switch-create-buffer (fn* (buffer-name) (if (buffer-exists buffer-name) (switch-buffer buffer-name) (do (create-buffer buffer-name) (switch-buffer buffer-name)))))", repl_env)
    rep("(write-buffer \"*Help*\" (ns))",repl_env)
    rep("(def! org-mode (fn* () (if (= (get-major-mode) \"org-mode\") (buffer-variable \"major-mode\" \"fundamental-mode\" (str (CURRENT-BUFFER))) (buffer-variable \"major-mode\" \"org-mode\" (str (CURRENT-BUFFER))))))", repl_env) // TODO: if current-buffer-major-mode == org-mode turn off
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

enum class WindowType {
    LEFT, RIGHT, TOP, BOTTOM, FULL
}

data class Window(var location: WindowType, var curentBuffer: KelBuffer, var focused: Boolean = false, var childWindow: Window? = null)

var WINDOWS = mutableStateListOf<Window>(Window(location = WindowType.FULL, curentBuffer = CURRENT_BUFFER.value))



@Composable
@Preview
fun App() {
    val contents = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    MaterialTheme {
        Column {
            val parentWindow = WINDOWS.first()
            WindowRenderer(parentWindow, focusRequester)
        }
    }
}


class MatchParenTransformation(
    val color: Color
) : VisualTransformation {
    override fun filter(atext: AnnotatedString): TransformedText {
        return TransformedText(
            buildAnnotatedStringWithUrlHighlighting(atext.toString(), color),
            OffsetMapping.Identity
        )
    }

    fun buildAnnotatedStringWithUrlHighlighting(
        text: String,
        color: Color
    ): AnnotatedString {
        return buildAnnotatedString {
            append(text)
            text?.split("\\s+".toRegex())?.filter { word ->
                word == "aa"
            }?.forEach {
                val startIndex = text.indexOf(it)
                val endIndex = startIndex + it.length
                addStyle(
                    style = SpanStyle(
                        color = color,
                        textDecoration = TextDecoration.None
                    ),
                    start = startIndex, end = endIndex
                )
            }
        }
    }
}

fun lispEscape(str: String): String {
    return str
        .replace("\\", "\\\\")   // Escape backslashes first
        .replace("\"", "\\\"")   // Then escape quotes
        .replace("\n", "\\n")    // Optional: Escape newlines if needed
}


@Composable
fun FundamentalMode(modifier: Modifier = Modifier, displayText: String, updateDisplayText: (String) -> Unit) {
    if (CURRENT_BUFFER.value.minorModes.contains("read-only-mode")) {
        LazyColumn(modifier = modifier.fillMaxHeight(.8f)) {
            items(displayText.lines().size, key= { it.hashCode() } ) { i ->
                Text(displayText.lines()[i])
            }
        }
    } else {
        TextField(
            value= displayText,
            onValueChange = {
                updateDisplayText(it)
                rep("(clear-buffer (CURRENT-BUFFER))", repl_env)
                val chunkSize = 1000
                displayText.chunked(chunkSize).forEach { chunk ->
                    rep("(append-to-buffer (CURRENT-BUFFER) \"" + lispEscape(chunk) + "\")", repl_env)
                }
            },
            modifier = modifier.fillMaxWidth().fillMaxHeight(1f)
        )
    }
}

@Composable
fun UIBufferSelector(window: Window, updateDisplayText: (String) -> Unit) {
    var selectedButtonBuffer by remember { mutableStateOf(window.curentBuffer.name) }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.SpaceEvenly) {
        BUFFERS.forEach { buff ->
            Button(
                onClick = {
                    selectedButtonBuffer = buff.name
                    window.curentBuffer = buff
                    updateDisplayText(buff.buf.snapshot().utf8())
                },
                colors = if (buff.name == selectedButtonBuffer) ButtonDefaults.buttonColors(MaterialTheme.colors.primary)
                else ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
            ) {
                Text(buff.name)
            }
        }
    }
}


@Composable
fun MiniBufferPrompt(updateDisplayText: (String) -> Unit) {
    var text by remember { mutableStateOf("")}
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = text,
            visualTransformation = MatchParenTransformation(Color.Blue),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onValueChange = { text = it },
            label = { Text("") },
            modifier = Modifier.weight(.80f)
        )

        Button(
            modifier = Modifier.weight(.20f).widthIn(50.dp, 100.dp),
            onClick = {
                HISTORY_BUFFER.buf.writeUtf8("${text}\n")
                MESSAGES_BUFFER.buf.writeUtf8("${rep(text, repl_env)}\n")
		updateDisplayText(CURRENT_BUFFER.value.buf.snapshot().utf8()) // TODO: is this right??
                text = if (textHook.value == null) { "" } else {
                    var autocomplete = textHook.value
                    textHook.value = ""
                    autocomplete
                }
            }) {
            Text("Eval")
        }
    }
}




@Composable
fun WindowRenderer(window: Window, focusRequester: FocusRequester) {
    var loading by remember { mutableStateOf(false)}

    LaunchedEffect(WINDOWS.size) {
        loading = true
        loading = false
    }
    // TODO: fix this, this is not letting window split happen in a split window
    if (window.childWindow != null) {
	when (window.childWindow!!.location) {
            WindowType.TOP -> {
                Column {
                    Row(modifier = Modifier.weight(0.5f)) {
                        MultiBufferExample(
                            modifier = Modifier.focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        window.focused = true
                                        println("${window} is focused!")
                                    } else {
                                        window.focused = false
                                        println("unfocused...")
                                    }
                                }, window.childWindow!!
                        )
                    }
                    Row(Modifier.weight(0.5f), verticalAlignment = Alignment.Bottom) {
                        MultiBufferExample(
                            modifier = Modifier.focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        window.focused = true
                                        println("${window} is focused!")
                                    } else {
                                        window.focused = false
                                        println("unfocused...")
                                    }
                                }, window
                        )
                    }
                }
            }
            WindowType.BOTTOM -> {
                Column {
                    Row(Modifier.weight(0.5f), verticalAlignment = Alignment.Top) {
                        MultiBufferExample(modifier = Modifier.focusRequester(focusRequester).onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                window.focused = true
                                println("${window} is focused!")
                            } else {
                                window.focused = false
                                println("unfocused...")
                            }
                        }, window)
                    }
                    Row(Modifier.weight(0.5f),  verticalAlignment = Alignment.Bottom) {
                        MultiBufferExample(modifier = Modifier.focusRequester(focusRequester).onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                window.focused = true
                                println("${window} is focused!")
                            } else {
                                window.focused = false
                                println("unfocused...")
                            }
                        }, window.childWindow!!)
                    }
                }
            }
            WindowType.LEFT -> {
                Row {
                    Column(Modifier.weight(0.5f), horizontalAlignment = Alignment.Start) {
                        MultiBufferExample(
                            modifier = Modifier.focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        window.focused = true
                                        println("${window} is focused!")
                                    } else {
                                        window.focused = false
                                        println("unfocused...")
                                    }
                                }, window.childWindow!!
                        )
                    }
                    Column(Modifier.weight(0.5f), horizontalAlignment = Alignment.End) {
                        MultiBufferExample(
                            modifier = Modifier.focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        window.focused = true
                                        println("${window} is focused!")
                                    } else {
                                        window.focused = false
                                        println("unfocused...")
                                    }
                                }, window
                        )
                    }
                }
            }
            WindowType.RIGHT -> {
                Row {
                    Column(Modifier.weight(0.5f), horizontalAlignment = Alignment.End) {
                        MultiBufferExample(
                            modifier = Modifier.focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        window.focused = true
                                        println("${window} is focused!")
                                    } else {
                                        window.focused = false
                                        println("unfocused...")
                                    }
                                }, window
                        )
                    }
                    Column(Modifier.weight(0.5f), horizontalAlignment = Alignment.Start) {
                        MultiBufferExample(
                            modifier = Modifier.focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        window.focused = true
                                        println("${window} is focused!")
                                    } else {
                                        window.focused = false
                                        println("unfocused...")
                                    }
                                }, window.childWindow!!
                        )
                    }
                }
            }
            else -> {
		MultiBufferExample(modifier = Modifier.focusRequester(focusRequester).onFocusChanged { focusState ->
				       if (focusState.isFocused) {
					   window.focused = true
					   println("${window} is focused!")
				       } else {
					   window.focused = false
					   println("unfocused...")
				       }
				   }, window)
	    }
	}
    } else {
        MultiBufferExample(modifier = Modifier.focusRequester(focusRequester).onFocusChanged { focusState ->
            if (focusState.isFocused) {
                window.focused = true
                println("${window} is focused!")
            } else {
                window.focused = false
                println("unfocused...")
            }
        }, window)
    }
}

@Composable
fun MultiBufferExample(modifier: Modifier = Modifier, window: Window) {
    var displayText by remember { mutableStateOf(window.curentBuffer.buf.snapshot().utf8()) }
    Column(modifier = modifier.padding(16.dp).fillMaxHeight().background(Color(0XFFD1D2D4))) {
        UIBufferSelector(window, { displayText = it })
        Row(modifier.weight(1f).fillMaxWidth()) {
            SelectionContainer(modifier) {
                when (window.curentBuffer.majorMode) {
                    "org-mode" -> {   OrgMode(modifier, displayText) }
                    "web-mode" -> { WebMode() }
                    else -> { FundamentalMode(modifier, displayText, {displayText = it}) }
                }
            }
        }
	MiniBufferPrompt({displayText = it})
    }
}
