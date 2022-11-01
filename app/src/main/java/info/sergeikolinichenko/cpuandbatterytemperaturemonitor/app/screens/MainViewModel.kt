package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.R
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.NUMBER_OF_DATA_READ_CYCLES
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.getFullDate
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.AddTemps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.ClearDb
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.GetAllTemps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.GetAllTempsLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*

/** Created by Sergei Kolinichenko on 25.10.2022 at 09:02 (GMT+3) **/

class MainViewModel(
    private val registerReceiver: Intent?,
    private val clearDb: ClearDb,
    private val addTemps: AddTemps,
    private val getAllTemps: GetAllTemps,
    getAllTempsLiveData: GetAllTempsLiveData
) : ViewModel() {

    private var _temps: List<Temps>? = null
    private val temps: List<Temps>
        get() = _temps ?: throw RuntimeException("List<Temps> equal null")

    val tempsList = getAllTempsLiveData.invoke()

    private var _showMessage = MutableLiveData<Int>()
    val showMessage: LiveData<Int>
        get() = _showMessage

    private var cycleWriteData: Boolean = true
    private val contStringDatabaseCleared = R.string.database_cleared
    private val contStringCsvFileSaved = R.string.csv_file_saved
    private val contStringCsvFileSaveFiled = R.string.csv_file_save_filed

        init {
        getTemperatures()
    }

    private fun getTemperatures() {
        viewModelScope.launch {
            while (cycleWriteData) {
                val tempCpu = getTempCpu()
                val timeStamp = System.currentTimeMillis()
                val tempBat = getTempBat()
                val temps = Temps(
                    timeStamp,
                    tempCpu,
                    tempBat
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
        _showMessage.value = contStringDatabaseCleared
    }

    private fun getTempBat(): String {
        val intent = registerReceiver
        return (intent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            0
        ))?.div(10).toString()
    }

    private fun getTempCpu(): String {
        val tempCpu = mutableListOf<String>()
        var temp: String?
        var type: String?
        for (count in 0 until NUMBER_OF_DATA_READ_CYCLES) {
            temp = getTemp(count)
            type = getType(count)
            type?.let {
                temp?.let {
                    if (temp.toFloat() > 0) {
                        tempCpu.add("$type $temp")
                    }
                }
            }
        }
        return tempCpu.joinToString(SEPARATOR)
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

    fun saveCsv() {
        cycleWriteData = false
        viewModelScope.launch {
            _temps = getAllTemps.invoke()
            kotlin.runCatching {
                val filePath = getFilePath()
                val csvFail = File(filePath, "temperatures.csv")
                if (!csvFail.exists()) {
                    csvFail.createNewFile()
                } else {
                    csvFail.delete()
                    csvFail.createNewFile()
                }
                val fileOutputStream = FileOutputStream(csvFail)
                val outputStreamWriter = OutputStreamWriter(fileOutputStream)
                try {
                    outputStreamWrite(outputStreamWriter, temps)
                    _showMessage.value = contStringCsvFileSaved
                } catch (e: Exception) {
                    _showMessage.value = contStringCsvFileSaveFiled
                } finally {
                    outputStreamWriter.close()
                    fileOutputStream.close()
                    cycleWriteData = true
                }
            }
        }
    }

    private fun getFilePath(): File {
        val filePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(path.absolutePath + "/" + "CSV_FILE")
        } else {
            val path = Environment.getExternalStorageDirectory()
            File(path.absolutePath + "/" + "CSV_FILE")
        }
        if (!filePath.exists()) {
            filePath.mkdir()
        }
        return filePath
    }

    private fun outputStreamWrite(
        outputStreamWriter: OutputStreamWriter,
        temps: List<Temps>
    ) {
        for (item in temps.indices) {
            val dateTime = temps[item].timeStamp.getFullDate()
            val tempCpu = temps[item].tempCpu
            val tempBat = "Battery ${temps[item].tempBat}"
            outputStreamWriter.append(
                "$dateTime," +
                        " $tempCpu," +
                        " $tempBat\n"
            )
        }
    }

    companion object {
        private const val INTERVAL = 1000L
        const val SEPARATOR = ", "
    }
}