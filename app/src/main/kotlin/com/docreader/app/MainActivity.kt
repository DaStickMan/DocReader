package com.docreader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DocReaderApplication).container

        setContent {
            DocReaderTheme {
                val viewModel: RecentFilesViewModel = viewModel(
                    factory = RecentFilesViewModel.factory(container),
                )
                RecentFilesScreen(viewModel = viewModel)
            }
        }
    }
}
