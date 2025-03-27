package org.kel.mics.IO

import okio.FileSystem
import okio.Path.Companion.toPath

actual fun readFileToStr(fpath: String): String {
    val path = fpath.toPath()

    val readmeContent = FileSystem.SYSTEM.read(path) {
        readUtf8()
    }
    return readmeContent
}

