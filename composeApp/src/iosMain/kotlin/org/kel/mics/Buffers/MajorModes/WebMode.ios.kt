package org.kel.mics.Buffers.MajorModes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration


@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun WebView(loaded: MutableState<Boolean>, failed: MutableState<Boolean>, url: String) {
    val webView = WKWebView(CGRectMake(0.0, 0.0, 500.0, 500.0),
        configuration = WKWebViewConfiguration())
    UIKitView(
        factory = {
            val nsUrl = NSURL(string = url)
            webView.scrollView.scrollEnabled = true
            webView.scrollView.alwaysBounceVertical = true
            webView.allowsBackForwardNavigationGestures = true

            if (nsUrl != null) {
                val request = NSURLRequest(nsUrl)
                webView.loadRequest(request)
            }
            webView
        },
        modifier = Modifier.fillMaxSize().padding(10.dp),
        update = { webView ->
            val nsUrl = NSURL(string = url)
            if (nsUrl != null) {
                val request = NSURLRequest(nsUrl)
                webView.loadRequest(request)
            }
        },
        // https://github.com/KevinnZou/compose-webview-multiplatform/issues/219
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true
        )
    )
    val scope = rememberCoroutineScope()
    scope.launch {
        (1..10).forEach {
            if (webView.title!!.isNotEmpty()) {
                loaded.value = true
                return@forEach
            }
            delay(1000)
        }
        if (webView.title.isNullOrBlank()) {
            failed.value = true
        }
    }
}

