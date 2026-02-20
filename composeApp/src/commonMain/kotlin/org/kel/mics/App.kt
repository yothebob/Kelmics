@file:OptIn(ExperimentalUuidApi::class)

package org.kel.mics

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BasicTooltipState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kelmics.composeapp.generated.resources.Res
import kotlinx.coroutines.launch
import okio.Buffer
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kel.mics.Buffers.KelBuffer
import org.kel.mics.Buffers.MajorModes.BufferListMode
import org.kel.mics.Buffers.MajorModes.ImageViewerMode
import org.kel.mics.Buffers.MajorModes.OrgDocument
import org.kel.mics.Buffers.MajorModes.OrgMode
import org.kel.mics.Buffers.MajorModes.WebMode
import org.kel.mics.Buffers.MajorModes.bufferListNs
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

var MESSAGES_BUFFER = KelBuffer(name="*Messages*")
var CURRENT_BUFFER = mutableStateOf<KelBuffer>(MESSAGES_BUFFER)
var HISTORY_BUFFER = KelBuffer(name="*History*")
var ASYNC_BUFFER = KelBuffer("*Remote-msg*")
var BUFFERS = mutableStateListOf<KelBuffer>(MESSAGES_BUFFER, ASYNC_BUFFER, HISTORY_BUFFER, KelBuffer("*Help*"))
var backgroundColor = mutableStateOf(0XFF15467B)
var textHook = mutableStateOf("")
var reload = mutableStateOf(false)


fun mmm () : Env {
    val repl_env = Env()
    ns.forEach({ it -> repl_env.set(it.key, it.value) })
    bufferNs.forEach({ it -> repl_env.set(it.key, it.value) })
    bufferListNs.forEach({ it -> repl_env.set(it.key, it.value) })
    // repl_env.set(MalSymbol("*ARGV*"), MalList(args.drop(1).map({ it -> MalString(it) }).toMutableList()))

    repl_env.set(MalSymbol("create-buffer"), MalFunction({ a: ISeq ->
        val nameCount = BUFFERS.count { it.name.replace(Regex("/<\\d+>$"), "") == a.first().getVal().toString() }
        val buffName = if (nameCount == 0) a.first().getVal().toString() else "${a.first().getVal().toString()}<${nameCount}>"
        val newBuffer = KelBuffer(name=buffName)
        BUFFERS.add(newBuffer)
        NIL
    })) // (create-buffer "name")
    repl_env.set(MalSymbol("switch-buffer"), MalFunction({ a: ISeq ->
        val foundBuffer = BUFFERS.firstOrNull { it.name == a.first().getVal().toString() }
        if (foundBuffer != null) {
            val focusedWindow =  WINDOWS.firstOrNull { it.focused }
            focusedWindow?.curentBuffer = foundBuffer
        }
        reload.value = true
        NIL
    })) //(switch-buffer "BUFFER_NAME")
    repl_env.set(MalSymbol("eval-buffer"), MalFunction({a : ISeq ->
        val focusedWindow = WINDOWS.first { w -> w.focused }
        MalString(rep(focusedWindow.curentBuffer.buf.snapshot().utf8(), repl_env))
    }))
    repl_env.set(MalSymbol("eval"), MalFunction({ a: ISeq -> eval(a.first(), repl_env) }))
    repl_env.set(MalSymbol("reload"), MalFunction({ a: ISeq -> reload.value = true; NIL}))
    repl_env.set(MalSymbol("ns"), MalFunction({ a: ISeq -> repl_env.showNamespace() }))
    repl_env.set(MalSymbol("show-variable"), MalFunction({ a: ISeq ->
        println(repl_env.get("test")?.mal_print())
        NIL
    }))

    rep("(def! not (fn* (a) (if a false true)))", repl_env)
    rep("(def! find-file (fn* (fname) (create-buffer fname (slurp fname))))", repl_env)
    rep("(def! load-file (fn* (f) (eval (read-string (str \"(do \" (slurp f) \"\nnil)\")))))", repl_env)
    rep("(defmacro! cond (fn* (& xs) (if (> (count xs) 0) (list 'if (first xs) (if (> (count xs) 1) (nth xs 1) (throw \"odd number of forms to cond\")) (cons 'cond (rest (rest xs)))))))", repl_env)
    rep("(def! switch-create-buffer (fn* (buffer-name) (if (buffer-exists buffer-name) (switch-buffer buffer-name) (do (create-buffer buffer-name) (switch-buffer buffer-name) (reload)))))", repl_env)
    rep("(write-buffer \"*Help*\" (ns))",repl_env)
    rep("(def! org-mode (fn* () (if (= (get-major-mode) \"org-mode\") (progn (buffer-variable \"major-mode\" \"fundamental-mode\" (str (CURRENT-BUFFER))) (reload)) (progn (buffer-variable \"major-mode\" \"org-mode\" (str (CURRENT-BUFFER))) (reload)))))", repl_env)
    rep("(def! web-mode (fn* () (if (= (get-major-mode) \"web-mode\") (progn (buffer-variable \"major-mode\" \"fundamental-mode\" (str (CURRENT-BUFFER))) (reload)) (progn (buffer-variable \"major-mode\" \"web-mode\" (str (CURRENT-BUFFER))) (reload)))))", repl_env)
    rep("(def! image-viewer-mode (fn* () (if (= (get-major-mode) \"image-viewer-mode\") (progn (buffer-variable \"major-mode\" \"fundamental-mode\" (str (CURRENT-BUFFER))) (reload)) (progn (buffer-variable \"major-mode\" \"image-viewer-mode\" (str (CURRENT-BUFFER))) (reload)))))", repl_env)
    return repl_env
}
val repl_env = mmm()

enum class WindowType {
    LEFT, RIGHT, TOP, BOTTOM, FULL
}

data class Window @OptIn(ExperimentalUuidApi::class) constructor(var location: WindowType, var curentBuffer: KelBuffer, var focused: Boolean = false, var childWindow: Window? = null, var uuid: Uuid = Uuid.random())
@OptIn(ExperimentalUuidApi::class)
var WINDOWS = mutableStateListOf<Window>(Window(location = WindowType.FULL, curentBuffer = CURRENT_BUFFER.value, focused = true))

@Composable
@Preview
fun App() {
    val contents = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    MaterialTheme {
        Column {
            val parentWindow = WINDOWS.first { it.focused }
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

// backlining this because I hope to phase it out...
//@Composable
//fun UIBufferSelector(window: Window, updateDisplayText: (String) -> Unit) {
//    var selectedButtonBuffer by remember { mutableStateOf(window.curentBuffer.name) }
//
//    LaunchedEffect(window) {
//        selectedButtonBuffer = window.curentBuffer.name
//        window.curentBuffer = buff
//    }
//
//    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.SpaceEvenly) {
//        BUFFERS.forEach { buff ->
//            Button(
//                onClick = {
//                    selectedButtonBuffer = buff.name
//                    window.curentBuffer = buff
//
//                    updateDisplayText(buff.buf.snapshot().utf8())
//                },
//                colors = if (buff.name == selectedButtonBuffer) ButtonDefaults.buttonColors(MaterialTheme.colors.primary)
//                else ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
//            ) {
//                Text(buff.name)
//            }
//        }
//    }
//}


@OptIn(ExperimentalMaterial3Api::class)
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
                if (text.first().toString() != "(") {
                    text = "(" + text + ")"
                }
                println(text)
                HISTORY_BUFFER.buf.writeUtf8("${text}\n")
                MESSAGES_BUFFER.buf.writeUtf8("${rep(text, repl_env)}\n")
                val currentWin = WINDOWS.firstOrNull { it.focused }
                if (currentWin?.curentBuffer?.majorMode !in listOf<String>("web-mode")) {
                    updateDisplayText(currentWin?.curentBuffer?.buf?.snapshot()?.utf8() ?: "")
                }
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

    LaunchedEffect(WINDOWS.size, window.curentBuffer) {
        println("274 LE: ${WINDOWS.size}, ${window.curentBuffer}")
        loading = true
        loading = false
    }
    // TODO: fix this, this is not letting window split happen in a split window
    if (window.childWindow != null) {
	when (window.childWindow!!.location) {
            WindowType.TOP -> {
                Column {
                    Row(modifier = Modifier.weight(0.5f)) {
                        WindowRenderer(window.childWindow!!, focusRequester)
                    }
                    Row(Modifier.weight(0.5f), verticalAlignment = Alignment.Bottom) {
                        MultiBufferExample(
                            modifier = Modifier.focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        window.focused = true
                                        WINDOWS.forEach { if (it.uuid != window.uuid) { it.focused = false }}
                                        println("${window} is focused!")
                                    } else {
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
                                WINDOWS.forEach { if (it.uuid != window.uuid) { it.focused = false }}
                                println("${window} is focused!")
                            } else {
                                println("unfocused...")
                            }
                        }, window)
                    }
                    Row(Modifier.weight(0.5f),  verticalAlignment = Alignment.Bottom) {
                        WindowRenderer(window.childWindow!!, focusRequester)
                    }
                }
            }
            WindowType.LEFT -> {
                Row {
                    Column(Modifier.weight(0.5f), horizontalAlignment = Alignment.Start) {
                        WindowRenderer(window.childWindow!!, focusRequester)
                    }
                    Column(Modifier.weight(0.5f), horizontalAlignment = Alignment.End) {
                        MultiBufferExample(
                            modifier = Modifier.focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        window.focused = true
                                        WINDOWS.forEach { if (it.uuid != window.uuid) { it.focused = false }}
                                        println("${window} is focused!")
                                    } else {
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
                                        WINDOWS.forEach { if (it.uuid != window.uuid) { it.focused = false }}
                                        println("${window} is focused!")
                                    } else {
                                        println("unfocused...")
                                    }
                                }, window
                        )
                    }
                    Column(Modifier.weight(0.5f), horizontalAlignment = Alignment.Start) {
                        WindowRenderer(window.childWindow!!, focusRequester)
                    }
                }
            }
            else -> {
		MultiBufferExample(modifier = Modifier.focusRequester(focusRequester).onFocusChanged { focusState ->
				       if (focusState.isFocused) {
                           window.focused = true
                           WINDOWS.forEach { if (it.uuid != window.uuid) { it.focused = false }}
                           println("${window} is focused!")
				       } else {
                           println("unfocused...")
				       }
				   }, window)
	    }
	}
    } else {
        MultiBufferExample(modifier = Modifier.focusRequester(focusRequester).onFocusChanged { focusState ->
            if (focusState.isFocused) {
                window.focused = true
                WINDOWS.forEach { if (it.uuid != window.uuid) { it.focused = false }}
                println("${window} is focused!")
            } else {
                println("unfocused...")
            }
        }, window)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiBufferExample(modifier: Modifier = Modifier, window: Window) {
    var displayText by remember { mutableStateOf("") }
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()


    LaunchedEffect(window.curentBuffer.majorMode, displayText, reload.value) {
        println("391: LE, ${window.curentBuffer.majorMode}")
        if (!reload.value) {
            return@LaunchedEffect
        }
//        displayText = window.curentBuffer.buf.snapshot().utf8()
        reload.value = false
    }

    Column(modifier = modifier.padding(16.dp).fillMaxHeight().background(Color(0XFFD1D2D4))) {
        // TODO: this is not turning off web-mode or switching buffers correctly
        Row(modifier.weight(1f).fillMaxWidth()) {
            SelectionContainer(modifier) {
                when (window.curentBuffer.majorMode) {
                    "org-mode" -> {   OrgMode(modifier, displayText) }
                    "web-mode" -> { WebMode() }
                    "image-viewer-mode" -> { ImageViewerMode() }
                    "buffer-list-mode" -> { BufferListMode({displayText = it}) }
                    else -> { FundamentalMode(modifier, displayText, {displayText = it}) }
                }
            }
        }

        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text("TODO: show different modes here to select from") } },
            state = tooltipState
        ) {
        }

        Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text(modifier = Modifier.clickable(
                    enabled = true,
                    onClick = {
                        rep("(do (next-buffer) (reload))", repl_env)
                        // rep(buffer-next)
                    }
                ), text = window.curentBuffer.name, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(30.dp))
                Text(modifier = Modifier.clickable(
                    enabled = true,
                    onClick = {
                        scope.launch {
                            tooltipState.show()
                        }
                    }
                ), text = window.curentBuffer.majorMode)
        }
	MiniBufferPrompt({displayText = it})
    }
}
