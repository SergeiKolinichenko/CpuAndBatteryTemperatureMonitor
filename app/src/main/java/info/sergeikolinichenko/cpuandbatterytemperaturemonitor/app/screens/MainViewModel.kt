package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.content.Intent
import android.os.BatteryManager
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.getFullDate
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.AddTemps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.ClearDb
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.GetAllTemps
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*

/** Created by Sergei Kolinichenko on 25.10.2022 at 09:02 (GMT+3) **/

class MainViewModel(
    private val registerReceiver: Intent?,
    private val clearDb: ClearDb,
    private val addTemps: AddTemps,
    private val getAllTemps: GetAllTemps
) : ViewModel() {

    private var _temps: List<Temps>? = null
    private val temps: List<Temps>
        get() = _temps ?: throw RuntimeException("List<Temps> equal null")

    private var _timeStamp = MutableLiveData<Long>()
    val timeStamp: LiveData<Long>
        get() = _timeStamp

    private var _tempCpu = MutableLiveData<Float>()
    val tempCpu: LiveData<Float>
        get() = _tempCpu

    private var _tempBat = MutableLiveData<Float>()
    val tempBat: LiveData<Float>
        get() = _tempBat

    private var _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

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
        _message.value = "Database cleared"
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

    fun saveCsv() {
        viewModelScope.launch {
            _temps = getAllTemps.invoke()

            kotlin.runCatching {
                val csvFail = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ), "file.csv"
                )
                if (!csvFail.exists()) {
                    csvFail.createNewFile()
                } else {
                    csvFail.delete()
                    csvFail.createNewFile()
                }
                val fileOutputStream = FileOutputStream(csvFail)
                val outputStreamWriter = OutputStreamWriter(fileOutputStream)
                try {
                    outputStreamWriter.append("Date and Time, CPU, Battery\n")
                    for (item in temps.indices) {
                        val dateTime = temps[item].timeStamp.getFullDate()
                        val tempCpu = temps[item].tempCpu.toString()
                        val tempBat = temps[item].tempBat.toString()
                        outputStreamWriter.append("$dateTime, $tempCpu, $tempBat\n")
                    }
                } catch (e: Exception) {
                    _message.value = "Writing csv file failed"
                }
                finally {
                    outputStreamWriter.close()
                    fileOutputStream.close()
                    _message.value = "The csv file is saved in the Download folder"
                }
            }
        }
    }

    companion object {
        private const val INTERVAL = 1000L
    }
}