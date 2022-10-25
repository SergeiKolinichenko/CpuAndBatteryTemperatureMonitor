package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.content.Intent
import android.os.BatteryManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.AddTemps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.ClearDb
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

/** Created by Sergei Kolinichenko on 25.10.2022 at 09:02 (GMT+3) **/

class MainViewModel(
    private val registerReceiver: Intent?,
    private val clearDb: ClearDb,
    private val addTemps: AddTemps
): ViewModel() {

    private var _timeStamp = MutableLiveData<Long>()
    val timeStamp: LiveData<Long>
    get() = _timeStamp

    private var _tempCpu = MutableLiveData<Float>()
    val tempCpu: LiveData<Float>
    get() = _tempCpu

    private var _tempBat = MutableLiveData<Float>()
    val tempBat: LiveData<Float>
        get() = _tempBat

    init {
        getTemperatures()
    }

    private fun getTemperatures() {
        viewModelScope.launch {
            while (true) {
                val timeStamp = System.currentTimeMillis()
                val tempCpu = getTempCpu()
                val tempBat = getTempBat()
                _timeStamp.value = timeStamp
                _tempCpu.value = tempCpu
                _tempBat.value = tempBat

                val temps = Temps(
                    timeStamp = timeStamp,
                    tempCpu = tempCpu,
                    tempBat = tempBat
                )

                addTemps.invoke(temps)

                delay(INTERVAL)
            }
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            clearDb.invoke()
        }
    }

    private fun getTempBat(): Float {
        val intent = registerReceiver
        return ((intent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            0
        ))?.toFloat() ?: 0F) / 10
    }

    private fun getTempCpu(): Float {
        val process: Process
        return try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp")
            process.waitFor()
            val reader =
                BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            line.toFloat() / 1000.0f
        } catch (e: Exception) {
            0.0f
        }
    }

    companion object {
        private const val INTERVAL = 1000L
    }
}