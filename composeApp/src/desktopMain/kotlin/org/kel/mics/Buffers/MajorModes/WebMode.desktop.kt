package org.kel.mics.Buffers.MajorModes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import org.kel.mics.reload


@androidx.compose.runtime.Composable
actual fun WebView( // TODO: this only works on first run of command, if you leave buffer and come back the screen is black
    loaded: androidx.compose.runtime.MutableState<kotlin.Boolean>,
    failed: androidx.compose.runtime.MutableState<kotlin.Boolean>,
    url: kotlin.String,
) {
    val fxPanel = remember { object : javafx.embed.swing.JFXPanel() { override fun getInputMethodRequests() = null} }

    SwingPanel(
        modifier = Modifier.fillMaxSize(),
        factory = {
            javafx.application.Platform.runLater {
                val webView = javafx.scene.web.WebView()
                webView.engine.load(url)
                fxPanel.scene = javafx.scene.Scene(webView)
            }
            fxPanel
        },
    )
}
