package org.kel.mics.Buffers.MajorModes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import org.kel.mics.BUFFERS
import org.kel.mics.Buffers.KelBuffer
import org.kel.mics.Mal.ISeq
import org.kel.mics.Mal.MalException
import org.kel.mics.Mal.MalFunction
import org.kel.mics.Mal.MalInteger
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalSymbol
import org.kel.mics.Mal.MalType
import org.kel.mics.Mal.NIL
import org.kel.mics.Mal.TRUE
import org.kel.mics.WINDOWS
import org.kel.mics.Window
import org.kel.mics.WindowType
import org.kel.mics.reload

@Composable
fun BufferListMode(updateDisplayText: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        BUFFERS.forEach { buff ->
            Row(Modifier.clickable(onClick = {
                println("running...")
                println(WINDOWS)
                val focusedWindow = WINDOWS.first { it.focused }
                focusedWindow.curentBuffer = buff
                updateDisplayText(buff.buf.snapshot().utf8())
                reload.value = true
            })) {
                Text(buff.name)
                Text(buff.majorMode)

            }
        }
    }
}

val bufferListNs = hashMapOf<MalSymbol, MalType>(
    MalSymbol("buffer-list-mode") to MalFunction({ a: ISeq -> // TODO: this works but does not initally re-render the buffer correctly
        val buffName = "*Buffer-List*"
        val foundBuffer = BUFFERS.firstOrNull { it.name == buffName }
        println(foundBuffer)
        println(WINDOWS)
        val focusedWindow = WINDOWS.firstOrNull { it.focused }
        println(focusedWindow)
        if (foundBuffer != null) {
            println(foundBuffer)
            foundBuffer.majorMode = "buffer-list-mode"
            focusedWindow?.curentBuffer = foundBuffer
        } else {
            val newBuffer = KelBuffer(name = buffName)
            newBuffer.majorMode = "buffer-list-mode"
            BUFFERS.add(newBuffer)
            focusedWindow?.curentBuffer = newBuffer
        }
        reload.value = true
        NIL
    })
)

