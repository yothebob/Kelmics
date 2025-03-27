package org.kel.mics.Buffers

enum class WindowTypeEnum {
    TEXT,
    PROMPT
}


class BufferCore() {

    var BUFFERS = listOf<Buffer>()
    var CURRENTBUFFER: Buffer = Buffer()

    init {
        println("init logging buffer...")
    }
}

class Buffer() {
    var name = ""
    var windowType = WindowTypeEnum.TEXT
    var bufferContents = "Hello world!"

    // update

    // save

    // hook
}