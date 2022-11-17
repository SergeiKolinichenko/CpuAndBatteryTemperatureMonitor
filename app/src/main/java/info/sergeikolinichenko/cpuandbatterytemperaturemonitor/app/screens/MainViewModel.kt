package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.content.ContentResolver
import android.content.Intent
import android.os.BatteryManager
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.R
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.ITEM_SEPARATOR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.NUMBER_OF_DATA_READ_CYCLES
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.STRING_SEPARATOR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainActivity.Companion.END_OF_LINE
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainActivity.Companion.SPACE
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.TimeUtils.getFullDate
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader
import java.io.OutputStreamWriter

/** Created by Sergei Kolinichenko on 25.10.2022 at 09:02 (GMT+3) **/

class MainViewModel(
    getAllTempsLiveData: GetAllTempsLiveData,
    private val registerReceiver: Intent?,
    private val clearDb: ClearDb,
    private val addTemps: AddTemps,
    private val getAllTemps: GetAllTemps,
    private val setMonitorStartStop: SetMonitorStartStop,
    getMonitorStartStop: GetMonitorStartStop,
) : ViewModel() {

    val tempsLiveData: LiveData<List<Temps>> = getAllTempsLiveData()

    private var _temps: List<Temps>? = null
    private val temps: List<Temps>
        get() = _temps ?: throw RuntimeException("List<Temps> equal null")

    private var _message = MutableLiveData<Int>()
    val message: LiveData<Int>
        get() = _message

    private var _intent = MutableLiveData<Intent>()
    val intent: LiveData<Intent>
        get() = _intent

    private var _cycleForMonitor = MutableLiveData<Boolean>()
    val cycleForMonitor: LiveData<Boolean>
        get() = _cycleForMonitor
    private val monStart: Boolean
        get() = cycleForMonitor.value ?: false

    val startStatusMonitorLV = MutableLiveData(getMonitorStartStop())
    private val startStatusMonitor
        get() = startStatusMonitorLV.value ?: false

    // Temperature monitoring start
    var startMonitoring = System.currentTimeMillis()
    private var _timeMonitoring = MutableLiveData<Long>()
    val timeMonitoring: LiveData<Long>
        get() = _timeMonitoring

    private var stateMonitoring = false

    private val stringFileSaved = R.string.csv_file_saved
    private val stringFileSaveFailed = R.string.csv_file_save_filed
    private val stringDataBaseCleared = R.string.database_cleared

    init {
        statusMonitoring(startStatusMonitor)
        getStartMonitoringTime()
    }

    fun setTimeStartMonitoring(time: Long) {
        startMonitoring = time
        getStartMonitoringTime()
    }

    private fun getStartMonitoringTime() {
        _timeMonitoring.value = startMonitoring
    }

    private fun getTemperatures() {
        viewModelScope.launch {
            while (monStart) {
                val timeStamp = System.currentTimeMillis()
                val tempCpu = getTempCpu()
                val tempBat = getTempBat()
                val temps = Temps(
                    timeStamp,
                    tempCpu,
                    tempBat
                )
                addTemps.invoke(temps)
                _timeMonitoring.value = timeStamp - startMonitoring
                delay(INTERVAL)
            }
        }
    }

    private fun getTempBat(): String {
        val intent = registerReceiver
        return (intent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            0
        ))?.div(10).toString()
    }

    private fun getTempCpu(): String {
        var tempCpu = ""
        for (count in 0 until NUMBER_OF_DATA_READ_CYCLES) {
            val temp = getTemp(count)
            val type = getType(count)
            type?.let {
                temp?.let {
                    if (temp.toFloat() > 0) {
                        tempCpu = tempCpu + type + ITEM_SEPARATOR + temp + STRING_SEPARATOR
                    }
                }
            }
        }
        val lastIndex = tempCpu.lastIndexOf(STRING_SEPARATOR)
        tempCpu = tempCpu.substring(0, lastIndex)
        return tempCpu
    }

    private fun getTemp(step: Int): String? {
        var result: String? = null
        var bufferedReader: BufferedReader? = null
        try {
            bufferedReader =
                BufferedReader(FileReader("/sys/class/thermal/thermal_zone$step/temp"))

            val line = bufferedReader.readLine().toFloat()

            val r = if (line > 10000) line / 1000
            else if (line > 1000) line / 100
            else if (line > 100) line / 10
            else line

            result = if (r > 0) {
                r.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bufferedReader?.close()
        }
        return result
    }

    private fun getType(step: Int): String? {
        var bufferedReader: BufferedReader? = null

        return try {
            bufferedReader =
                BufferedReader(FileReader("/sys/class/thermal/thermal_zone$step/type"))
            bufferedReader.readLine()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            bufferedReader?.close()
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            clearDb.invoke()
        }

        startMonitoring = System.currentTimeMillis()
        getStartMonitoringTime()

        _message.value = stringDataBaseCleared
    }

    fun setMonitorMode(mode: Boolean) {
        statusMonitoring(mode)
        if (mode) {
            clearDatabase()
        }
        setMonitorStartStop.invoke(mode)
    }

    private fun statusMonitoring(status: Boolean) {
        _cycleForMonitor.value = status
        if (status) {
            getTemperatures()
        }
    }

    fun saveFileStart() {
        stateMonitoring = cycleForMonitor.value ?: false // save monitoring status
        statusMonitoring(STOP_MONITORING)               // stop monitoring

        viewModelScope.launch {
            _temps = getAllTemps.invoke()
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        _intent.value = intent

    }

    fun saveFileEnd(file: DocumentFile?, cr: ContentResolver) {
        var result: Boolean
        var myFile = file?.findFile(NAME_FILE)
        if (myFile != null) {
            if (myFile.exists()) {
                myFile.delete()
                myFile = file?.createFile(MIME_TYPE, NAME_FILE)
            }
        } else {
            myFile = file?.createFile(MIME_TYPE, NAME_FILE)
        }
        val os = myFile?.let { cr.openOutputStream(it.uri) }
        val osw = OutputStreamWriter(os)
        os.use {
            result = outputDataWrite(osw)
        }
        statusMonitoring(stateMonitoring)           // restore monitoring status
        _message.value = if (result) stringFileSaved
        else stringFileSaveFailed
    }

    private fun outputDataWrite(
        osw: OutputStreamWriter
    ): Boolean {
        var result = false
        osw.use {
            _temps?.let {
                for (item in temps.indices) {
                    val dateTime = temps[item].timeStamp.getFullDate()
                    val tempCpu = temps[item].tempCpu.replace(ITEM_SEPARATOR, SPACE)
                    val tempBat = "Battery ${temps[item].tempBat}"
                    osw.append(
                        "$dateTime $tempCpu $tempBat $END_OF_LINE"
                    )
                }
                result = true
            }
        }
        return result
    }

    companion object {
        private const val INTERVAL = 1000L
        private const val NAME_FILE = "temperatures.csv"
        private const val MIME_TYPE = "*/txt"

        private const val START_MONITORING = true
        private const val STOP_MONITORING = false
    }
}