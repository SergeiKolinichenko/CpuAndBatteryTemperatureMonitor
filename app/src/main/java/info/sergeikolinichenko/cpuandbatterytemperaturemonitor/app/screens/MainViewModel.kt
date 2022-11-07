package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.content.ContentResolver
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
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

    private val stringFileSavedCsvDirectory = R.string.csv_file_saved_csv_directory
    private val stringFileSaved = R.string.csv_file_saved
    private val stringFileSaveFailed = R.string.csv_file_save_filed
    private val stringDataBaseCleared = R.string.database_cleared

    private var cycleWriteData: Boolean = true

    init {
        getTemperatures()
    }

    private fun getTemperatures() {
        viewModelScope.launch {
            while (cycleWriteData) {
                val timeStamp = System.currentTimeMillis()
                val tempCpu = getTempCpu()
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
        _message.value = stringDataBaseCleared
    }

    private fun getTempBat(): String {
        val intent = registerReceiver
        return (intent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            0
        ))?.div(10).toString()
    }

    private fun getTempCpu(): String {
        val tempCpu = StringBuilder()
        for (count in 0 until NUMBER_OF_DATA_READ_CYCLES) {
            val temp = getTemp(count)
            val type = getType(count)
            type?.let {
                temp?.let {
                    if (temp.toFloat() > 0) {
                        tempCpu.append(type)
                        tempCpu.append(ITEM_SEPARATOR)
                        tempCpu.append(temp)
                        tempCpu.append(STRING_SEPARATOR)
                    }
                }
            }
        }
        return tempCpu.toString()
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

    fun saveToFileCsv() {
        cycleWriteData = false
        viewModelScope.launch {
            _temps = getAllTemps.invoke()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveFileFromQStart()
        } else {
            saveFileBellowQ()
        }
        cycleWriteData = true
    }

    private fun saveFileFromQStart() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        _intent.value = intent
    }

    fun saveFileFromQEnd(file: DocumentFile?, cr: ContentResolver) {
        var result: Boolean
        val myFile = file?.createFile("*/csv", NAME_FILE)
        val os = myFile?.let { cr.openOutputStream(it.uri) }
        val osw = OutputStreamWriter(os)
        os.use {
            result = outputDataWrite(osw)
        }
        _message.value = if (result) stringFileSaved
        else stringFileSaveFailed
    }

    private fun saveFileBellowQ() {
        val path = Environment.getExternalStorageDirectory()
        val filePath = File(path.absolutePath + "/" + "CSV_FILE")
        var result: Boolean

        if (!filePath.exists()) {
            filePath.mkdir()
        }

        val file = File(filePath, NAME_FILE)
        val fos = FileOutputStream(file)
        fos.use {
            val osw = OutputStreamWriter(fos)
            result = outputDataWrite(osw)
        }
        _message.value = if (result) stringFileSavedCsvDirectory
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
    }
}