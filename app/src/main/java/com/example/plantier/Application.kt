package com.example.plantier

import android.app.Application
import com.juul.kable.Scanner
import com.pison.core.client.PisonRemoteServer
import com.pison.core.client.bindToLocalServer
import com.pison.core.client.newPisonSdkInstance
import kotlinx.coroutines.flow.first

class Application : Application() {
    companion object {
        lateinit var sdk: PisonRemoteServer
//        lateinit var clueDevice:
    }

    override fun onCreate() {
        super.onCreate()
        sdk = newPisonSdkInstance(applicationContext).bindToLocalServer()

    }

//
//    val advertisement = Scanner()
//        .advertisements
//        .first { it.name?.startsWith("Example") }
//
//    val clue = scope.peripheral(advertisement)
}