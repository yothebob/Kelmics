package org.kel.mics

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import okio.FileSystem
import java.util.Timer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

class KelmicsApp: Application() {
    companion object {
        lateinit var appCtx: Context

    }

    override fun onCreate() {
        super.onCreate()
        appCtx = applicationContext
    }
}


//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}