package org.kel.mics.Buffers

import okio.Buffer

enum class WindowTypeEnum {
    TEXT,
    PROMPT
}

class KelBuffer(name: String, windowType: WindowTypeEnum = WindowTypeEnum.TEXT) {
    var name = name
    var windowType = windowType
    var buf = Buffer()
    var fileName = "" // TODO: Look at cled on how this was done.
    var majorMode = "fundamental-mode"
    var minorModes = mutableListOf("")

    // update

    // save

    // hook
}
