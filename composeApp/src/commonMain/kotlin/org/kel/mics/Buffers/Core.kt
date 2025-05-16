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
    var majorMode = ""// TODO: This should take something (a new mal type?) that can be expanded and changes how the page is rendered/ behaves
    var minorModes = mutableListOf("")

    // update

    // save

    // hook
}
