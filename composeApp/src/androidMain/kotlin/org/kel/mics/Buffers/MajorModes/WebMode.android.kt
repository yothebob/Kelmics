package org.kel.mics.Buffers.MajorModes

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.kel.mics.KelmicsApp

class ACWebViewClient(var loaded: MutableState<Boolean>): WebViewClient() {
//    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//        view?.loadUrl(request?.url.toString())
//        return super.shouldOverrideUrlLoading(view, request)
//    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        println("stared loading")
        println(loaded.value)
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        println("Done loading")
        loaded.value = true
        println(loaded.value)
        super.onPageFinished(view, url)
    }
}

@Composable
actual fun WebView(loaded: MutableState<Boolean>, failed: MutableState<Boolean>, url: String) {
    val webView = android.webkit.WebView(KelmicsApp.appCtx).apply {
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        webViewClient = ACWebViewClient(loaded)
    }
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        AndroidView(factory = { webView }) { view ->
            view.loadUrl(
                url
            )
        }
    }
}
