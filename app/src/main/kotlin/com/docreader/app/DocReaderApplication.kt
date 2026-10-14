package com.docreader.app

import android.app.Application

class DocReaderApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
