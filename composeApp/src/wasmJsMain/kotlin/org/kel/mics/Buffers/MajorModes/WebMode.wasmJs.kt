package org.kel.mics.Buffers.MajorModes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
actual fun WebView(
    loaded: MutableState<Boolean>,
    failed: MutableState<Boolean>,
    url: String,
) {
}