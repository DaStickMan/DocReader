package com.docreader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DocReaderApplication).container

        setContent {
            DocReaderTheme {
                MainScreen(container = container)
            }
        }
    }
}
