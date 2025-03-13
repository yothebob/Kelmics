package org.kel.mics.IO

actual fun readFileToStr(fpath: String): String {
    // TODO: This cannot hook into the native filesystem, BUT I can do two things for the platforms that have this
    // 1. Store fake files in memory with okio FakeFileSystem, establish a file protocol for that
    // 2. Have a Remote filesystem protocol so it will work with a filesystem from a connected server from afar.. likely using the server app here with network calls.
    return ""
}
