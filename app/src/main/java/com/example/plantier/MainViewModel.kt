package com.example.plantier

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.subscribe
import com.example.plantier.Application.Companion.sdk
import com.juul.kable.*
import com.pison.core.client.PisonRemoteDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainViewModel : ViewModel() {
    private val scanScope = viewModelScope
    private val scanner = Scanner()

    private var disposable = Disposable()

    private val soilCharacteristic = characteristicOf(
        "6E400001-B5A3-F393-E0A9-E50E24DCCA9E",
        "6E400003-B5A3-F393-E0A9-E50E24DCCA9E",
    )

    private val motorCharacteristic = characteristicOf(
        "6E400001-B5A3-F393-E0A9-E50E24DCCA9E",
        "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
    )

    suspend fun onSoilIsDryReceived(pisonRemoteDevice: PisonRemoteDevice, cluePeripheral: Peripheral) {
        //TODO send haptic here

        disposable  = pisonRemoteDevice.monitorEulerAngles().subscribe {
            Log.d("DEBUG", "EULER Pitch${it.pitch} Roll:${it.roll}, Yaw: ${it.yaw}")
            if(it.roll < 0 && it.roll > -90 && it.pitch > 0 && it.pitch <90){
                scanScope.launch {
                    triggerWatering(cluePeripheral)
                }
            }
        }
    }

    suspend fun triggerWatering(cluePeripheral: Peripheral) {
        sendWateringCommandToClue(scanScope,cluePeripheral,motorCharacteristic,"Water plant")
        disposable.dispose()
    }

    fun scanForClue() {
        Log.d("DEBUG", "scanforcluecall")
        scanScope.launch {
            Log.d("DEBUG", "launched scope")
            val clueAdvertisement = scanner
                .advertisements
                .catch { cause ->
                    Log.d("DEBUG", "ERROR CATCH")
                }
                .onCompletion { cause ->
                    if (cause == null) Log.d("DEBUG", "SCAN COMPLETED")
                }
                .filter { it.isClue }
                .firstOrNull() ?: return@launch

            onClueFound(clueAdvertisement)
        }
    }

    suspend fun onClueFound(clueAdvertisement: Advertisement) {
        val cluePeripheral = createPeripheral(scanScope,clueAdvertisement)
        Log.d("DEBUG", "Clue found: $clueAdvertisement and Clue peripheral: $cluePeripheral")
        connectToClue(createPeripheral(scanScope,clueAdvertisement))
    }

    suspend fun connectToClue(cluePeripheral: Peripheral) {
        cluePeripheral.connect()
        sdk.monitorAnyDevice().subscribe {
            Log.d("DEBUG", "DEVICE CONNECTED")
            scanScope.launch {
                listenForClueUpdates(cluePeripheral,soilCharacteristic,it)
            }
        }
    }

    suspend fun listenForClueUpdates(
        cluePeripheral: Peripheral,
        characteristic: Characteristic,
        pisonRemoteDevice: PisonRemoteDevice
    ) {
        cluePeripheral.observe(characteristic).collect {
            Log.d("DEBUG", "READING DATA: ${it.toString()}")
            onSoilIsDryReceived(pisonRemoteDevice,cluePeripheral)
        }
    }

    suspend fun sendWateringCommandToClue(
        scope: CoroutineScope,
        peripheral: Peripheral,
        characteristic: Characteristic,
        string: String
    ) {
        peripheral.write(characteristic, string.toByteArray())
    }


    private fun createPeripheral(
        scanScope: CoroutineScope,
        advertisement: Advertisement
    ): Peripheral {
        Log.d("DEBUG", "Created peripheral")
        return scanScope.peripheral(advertisement)
    }

    private fun isItAClue(it: Advertisement): Boolean {
        Log.d("DEBUG", "Checking ${it.name}")
        return it.isClue
    }
}

private val Advertisement.isClue
    get() = name?.startsWith("CIRCUIT") == true

private fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

private fun CoroutineScope.cancelChildren(
    cause: CancellationException? = null
) = coroutineContext[Job]?.cancelChildren(cause)



