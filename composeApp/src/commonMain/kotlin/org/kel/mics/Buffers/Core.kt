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

    // update

    // save

    // hook
}