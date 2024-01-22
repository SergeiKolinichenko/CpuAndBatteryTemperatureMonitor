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
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.TimeUtils.getFullDate
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.AddTemps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.ClearDb
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.GetAllTemps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.GetAllTempsLiveData
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.GetStateMonitoring
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.GetTimeStartMonitoring
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.SetStateMonitoring
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.SetTimeStartMonitoring
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader
import java.io.OutputStreamWriter
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 25.10.2022 at 09:02 (GMT+3) **/

class MainViewModel @Inject constructor(
  getAllTempsLiveData: GetAllTempsLiveData,
  private val registerReceiver: Intent?,
  private val clearDb: ClearDb,
  private val addTemps: AddTemps,
  private val getAllTemps: GetAllTemps,
  private val setMonitorStartStop: SetStateMonitoring,
  getMonitorStartStop: GetStateMonitoring,
  private val setTimeStartMonitoring: SetTimeStartMonitoring,
  private val getTimeStartMonitoring: GetTimeStartMonitoring
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

  private var _monitorCycleOnOff = MutableLiveData<Boolean>()
  val monitorCycleOnOff: LiveData<Boolean>
    get() = _monitorCycleOnOff
  private val cycleOnOff: Boolean
    get() = monitorCycleOnOff.value ?: true

  val monitorStatStartLD = MutableLiveData(getMonitorStartStop())
  private val monitorStatStart
    get() = monitorStatStartLD.value ?: false

  private var _errorApp = MutableLiveData(false)
  val errorApp: LiveData<Boolean>
    get() = _errorApp

  // Temperature monitoring start
  private var _monitorStartTime = System.currentTimeMillis()
  val monitorStartTime: Long
    get() = _monitorStartTime

  private var _monitorDuration = MutableLiveData<Long>()
  val monitorDuration: LiveData<Long>
    get() = _monitorDuration

  private var monitorState = false

  private val stringFileSaved = R.string.csv_file_saved
  private val stringFileSaveFailed = R.string.csv_file_save_filed
  private val stringDataBaseCleared = R.string.database_cleared

  init {
    setMonitorStatus(monitorStatStart)
  }

  fun setStartMonitorTime(time: Long) {
    _monitorStartTime = time
    setTimeStartMonitoring.invoke(monitorStartTime)
  }

  fun getStartMonitorTime() {
    val start = getTimeStartMonitoring.invoke()
    _monitorStartTime = if (start > -1) {
      start
    } else {
      System.currentTimeMillis()
    }
  }

  fun setMonitorMode(mode: Boolean) {
    setMonitorStatus(mode)
    setMonitorStartStop.invoke(mode)
  }

  private fun setMonitorStatus(status: Boolean) {
    _monitorCycleOnOff.value = status
    if (status) {
      getTemperatures()
    }
  }

  fun clearDatabase() {
    viewModelScope.launch {
      clearDb.invoke()
    }
    val timeStamp = System.currentTimeMillis()
    setStartMonitorTime(timeStamp)
    _message.value = stringDataBaseCleared
  }

  private fun getTemperatures() {
    viewModelScope.launch {
      while (cycleOnOff) {
        val timeStamp = System.currentTimeMillis()
        val tempCpu = getTempCpu()
        val tempBat = getTempBat()
        if (tempCpu == EMPTY_STRING) {
          errorApp()
          return@launch
        }
        val temps = Temps(
          timeStamp,
          tempCpu,
          tempBat
        )
        addTemps.invoke(temps)
        _monitorDuration.value = timeStamp - monitorStartTime
        delay(INTERVAL)
      }
    }
  }

  private fun errorApp() {
    _monitorCycleOnOff.value = false
    _errorApp.value = true
  }

  private fun getTempBat(): String {
    val intent = registerReceiver
    return (intent?.getIntExtra(
      BatteryManager.EXTRA_TEMPERATURE,
      0
    ))?.div(10).toString()
  }

  private fun getTempCpu(): String {
    var tempCpu = EMPTY_STRING
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
    if (tempCpu != "") {
      val lastIndex = tempCpu.lastIndexOf(STRING_SEPARATOR)
      tempCpu = tempCpu.substring(0, lastIndex)
    }
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

  fun saveFileStart() {
    if (cycleOnOff) {
      monitorState = cycleOnOff           // save monitoring status
      setMonitorStatus(STOP_MONITORING)   // stop monitoring
    }

    viewModelScope.launch {
      _temps = getAllTemps.invoke()
    }

    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    _intent.value = intent

  }

  fun saveFileEnd(file: DocumentFile?, cr: ContentResolver?, resultOk: Boolean) {
    if (!resultOk) {
      _message.value = stringFileSaveFailed
      return
    }
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
    val os = myFile?.let { cr?.openOutputStream(it.uri) }
    val osw = OutputStreamWriter(os)
    os.use {
      result = outputDataWrite(osw)
    }

    if (monitorState) {
      setMonitorStatus(monitorState)           // restore monitoring status
    }

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
    private const val STOP_MONITORING = false
    private const val EMPTY_STRING = ""
    const val END_OF_LINE = "\n"
    const val SPACE = " "
    const val ACTIVITY_FOR_RESULT_OK = true
    const val ACTIVITY_FOR_RESULT_FAIL = false
  }
}