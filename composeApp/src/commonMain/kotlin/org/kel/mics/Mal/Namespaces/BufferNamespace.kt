@file:OptIn(ExperimentalUuidApi::class)

package org.kel.mics.Mal.Namespaces

import androidx.compose.runtime.toMutableStateList
import org.kel.mics.BUFFERS
import org.kel.mics.Buffers.KelBuffer
import org.kel.mics.CURRENT_BUFFER
import org.kel.mics.Mal.ISeq
import org.kel.mics.Mal.MalException
import org.kel.mics.Mal.MalFunction
import org.kel.mics.Mal.MalInteger
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalSymbol
import org.kel.mics.Mal.MalType
import org.kel.mics.Mal.NIL
import org.kel.mics.Mal.TRUE
import org.kel.mics.Mal.rep
import org.kel.mics.WINDOWS
import org.kel.mics.Window
import org.kel.mics.WindowType
import org.kel.mics.reload
import kotlin.uuid.ExperimentalUuidApi

val bufferNs = hashMapOf<MalSymbol, MalType>(
    MalSymbol("read-buffer") to MalFunction({a : ISeq ->
        val buffName = a.first() as? MalString ?: throw MalException("takes a str arg")
        val buffToRead = BUFFERS.first { buffName.value == it.name }
        MalString(buffToRead.buf.snapshot().utf8())
    }, docs="(read-buffer BUFFNAME:MALSTRING) -> MALSTRING \n  reads BUFFNAME buffer and returns contents"),

    MalSymbol("write-buffer") to MalFunction({a : ISeq ->
        val buffName = a.first() as? MalString ?: throw MalException("takes a str arg")
        val buffContents = a.nth(1) as? MalString ?: throw MalException("takes a str arg")
        val buffToWrite = BUFFERS.first { buffName.value == it.name }
        println("buffContents, ${buffContents.value}")
        buffToWrite.buf.writeUtf8(buffContents.value)
        NIL
    }, docs="(write-buffer BUFFNAME:MALSTRING BUFFCONTENTS:MALSTRING) -> NIL \n  writes BUFFCONTENTS to BUFFNAME buffer and returns nil"),
    MalSymbol("delete-buffer") to MalFunction({a : ISeq ->
        val buffName = a.first() as? MalString ?: throw MalException("takes a str arg")
        BUFFERS = BUFFERS.filter { it.name != buffName.value }.toMutableStateList()
        NIL
    }),
    MalSymbol("append-to-buffer") to MalFunction({a : ISeq ->
        val buffName = a.first() as? MalString ?: throw MalException("takes a str arg")
        val buffer = BUFFERS.first { buffName.value == it.name }
        val contents = a.nth(1) as? MalString ?: throw MalException("takes a str arg")
        buffer.buf.writeUtf8(contents.value)
        NIL
    }, docs = "(append-to-buffer BUFFNAME:MALSTRING CONTENTS:MALSTRING) -> NIL \n  Takes CONTENTS and appends it to BUFFNAME."),
    MalSymbol("buffer-exists") to MalFunction({a : ISeq ->
        val buffName = a.first() as? MalString ?: throw MalException("takes a str arg")
        val buffToRead = BUFFERS.any { buffName.value == it.name }
        println(buffToRead)
        if (buffToRead) TRUE else NIL
    }),
    MalSymbol("clear-buffer") to MalFunction({a : ISeq ->
        val buffName = a.first() as? MalString ?: throw MalException("takes a str arg")
        val buffToWrite = BUFFERS.first { buffName.value == it.name }
        buffToWrite.buf.clear()
        NIL
    }, docs="(clear-buffer BUFFNAME:MALSTRING) -> NIL \n  Clears BUFFNAME"),
    MalSymbol("delete-buffer") to MalFunction({a : ISeq ->
        val buffName = a.first() as? MalString ?: throw MalException("takes a str arg")
        BUFFERS = BUFFERS.filter { it.name != buffName.value }.toMutableStateList()
        NIL
    }),
    MalSymbol("read-region") to MalFunction({a : ISeq ->
        val min =  a.first() as? MalInteger ?: throw MalException("takes a int arg")
        val max =  a.nth(1) as? MalInteger ?: throw MalException("takes a int arg")
        val focusedWindow = WINDOWS.first { w -> w.focused == true }
        val clone = focusedWindow.curentBuffer.buf.peek()
        clone.skip(min.value)
        MalString(clone.readByteString(max.value - min.value).toString())
    }, docs="(read-region MIN:MALSTRING MAX:MALSTRING) -> MALSTRING\n  read content from a buffer from MIN to MAX position"),
    MalSymbol("buffer-variable") to MalFunction({a : ISeq -> // TODO: fix this so you can remove
        val bufferVar =  a.first() as? MalString ?: throw MalException("takes a string arg")
        val bufferArg =  a.nth(1) as? MalString ?: throw MalException("takes a string arg")
        val buffer =  a.nth(2) as? MalString ?: throw MalException("takes a string arg")
        val foundBuffer = BUFFERS.firstOrNull { buffer.value == it.name }
        when (bufferVar.value) {
            "major-mode" -> foundBuffer?.majorMode = bufferArg.value
            "minor-mode" -> foundBuffer?.minorModes?.add(bufferArg.value)
            "remove-minor-mode" -> foundBuffer?.minorModes?.remove(bufferArg.value)
            else -> { println("$foundBuffer was not found: input name ${buffer}") }
        }
        NIL
    }, docs="(buffer-variable BUFFERVAR:MALSTRING bufferArg:MALSTRING BUFFERNAME:MALSTRING) -> NIL\n  update a buffer (found by BUFFERNAME) variable, specified by BUFFERVAR"),
    MalSymbol("get-major-mode") to MalFunction({a : ISeq ->
        val focusedWindow = WINDOWS.first { w -> w.focused == true }
        val curBuffName =  a.firstOrNull() as? MalString ?: MalString(focusedWindow.curentBuffer.name)
        println("CurBuffName: $curBuffName")
        val foundBuffer = BUFFERS.first { curBuffName.value == it.name }
        println("foundBuffer: $foundBuffer")
        MalString(foundBuffer.majorMode)
    }, docs="(get-major-mode BUFFERNAME:MALSTRING) -> MALSTRING\n get BUFFERNAME major-mode or get CURRENT-BUFFER"),
    MalSymbol("new-window-below") to MalFunction({ a: ISeq ->
        val focusedWindow = WINDOWS.first { w -> w.focused == true }
        focusedWindow.location = WindowType.TOP
        val belowWindow = Window(location = WindowType.BOTTOM, focused = false, curentBuffer = focusedWindow.curentBuffer)
        WINDOWS.add(belowWindow)
	    focusedWindow.childWindow = belowWindow
        NIL
    }),
    MalSymbol("new-window-right") to MalFunction({ a: ISeq ->
        val focusedWindow = WINDOWS.first { w -> w.focused == true }
        focusedWindow.location = WindowType.LEFT
        val rightWindow = Window(location = WindowType.RIGHT, focused = false, curentBuffer = focusedWindow.curentBuffer)
        WINDOWS.add(rightWindow)
	    focusedWindow.childWindow = rightWindow
        NIL
    }),
    MalSymbol("delete-other-windows") to MalFunction({ a: ISeq ->
        val focusedWindow = WINDOWS.first { w -> w.focused == true }
        focusedWindow.location = WindowType.FULL
	focusedWindow.childWindow = null
        WINDOWS.removeAll{ w -> !w.focused }
        NIL
    }),
    MalSymbol("next-buffer") to MalFunction({ a: ISeq ->

        val focusedWindow = WINDOWS.first { w -> w.focused == true }
        val bufferIdx = BUFFERS.indexOf(focusedWindow.curentBuffer)
        if ((bufferIdx + 1) >= (BUFFERS.size - 1)) {
            focusedWindow.curentBuffer = BUFFERS[0]
        } else {
            focusedWindow.curentBuffer = BUFFERS[bufferIdx + 1]
        }
        NIL
    }),
    MalSymbol("CURRENT-BUFFER") to MalFunction({ a: ISeq ->
        val focusedWindow = WINDOWS.first { w -> w.focused == true }
        MalString(focusedWindow.curentBuffer.name)
    })
)
