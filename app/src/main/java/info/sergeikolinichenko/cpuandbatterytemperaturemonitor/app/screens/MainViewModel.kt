package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.util.Log
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

    private var _tempCpu0 = MutableLiveData<String>()
    val tempCpu0: LiveData<String>
        get() = _tempCpu0

    private var _tempCpu1 = MutableLiveData<String>()
    val tempCpu1: LiveData<String>
        get() = _tempCpu1

    private var _tempCpu2 = MutableLiveData<String>()
    val tempCpu2: LiveData<String>
        get() = _tempCpu2

    private var _tempCpu3 = MutableLiveData<String>()
    val tempCpu3: LiveData<String>
        get() = _tempCpu3

    private var _tempCpu4 = MutableLiveData<String>()
    val tempCpu4: LiveData<String>
        get() = _tempCpu4

    private var _tempCpu5 = MutableLiveData<String>()
    val tempCpu5: LiveData<String>
        get() = _tempCpu5

    private var _tempCpu6 = MutableLiveData<String>()
    val tempCpu6: LiveData<String>
        get() = _tempCpu6

    private var _tempCpu7 = MutableLiveData<String>()
    val tempCpu7: LiveData<String>
        get() = _tempCpu7

    private var _tempCpu8 = MutableLiveData<String>()
    val tempCpu8: LiveData<String>
        get() = _tempCpu8

    private var _tempCpu9 = MutableLiveData<String>()
    val tempCpu9: LiveData<String>
        get() = _tempCpu9

    private var _tempCpu10 = MutableLiveData<String>()
    val tempCpu10: LiveData<String>
        get() = _tempCpu10

    private var _tempCpu11 = MutableLiveData<String>()
    val tempCpu11: LiveData<String>
        get() = _tempCpu11

    private var _tempCpu12 = MutableLiveData<String>()
    val tempCpu12: LiveData<String>
        get() = _tempCpu12

    private var _tempCpu13 = MutableLiveData<String>()
    val tempCpu13: LiveData<String>
        get() = _tempCpu13

    private var _tempCpu14 = MutableLiveData<String>()
    val tempCpu14: LiveData<String>
        get() = _tempCpu14

    private var _tempCpu15 = MutableLiveData<String>()
    val tempCpu15: LiveData<String>
        get() = _tempCpu15

    private var _tempCpu16 = MutableLiveData<String>()
    val tempCpu16: LiveData<String>
        get() = _tempCpu16

    private var _tempCpu17 = MutableLiveData<String>()
    val tempCpu17: LiveData<String>
        get() = _tempCpu17

    private var _tempCpu18 = MutableLiveData<String>()
    val tempCpu18: LiveData<String>
        get() = _tempCpu18

    private var _tempCpu19 = MutableLiveData<String>()
    val tempCpu19: LiveData<String>
        get() = _tempCpu19

    private var _tempBat = MutableLiveData<String>()
    val tempBat: LiveData<String>
        get() = _tempBat

    private var _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    private var cycleWriteData: Boolean = true

    init {
        getTemperatures()
    }

    private fun getTemperatures() {
        viewModelScope.launch {
            while (cycleWriteData) {
                val array = getTempsCpu()
                Log.d("MyLog", "array.toString() ${array}")
                val timeStamp = System.currentTimeMillis()
                val tempBat = getTempBat()

                _tempBat.value = tempBat

                _tempCpu0.value = array[0]
                _tempCpu1.value = array[1]
                _tempCpu2.value = array[2]
                _tempCpu3.value = array[3]
                _tempCpu4.value = array[4]
                _tempCpu5.value = array[5]
                _tempCpu6.value = array[6]
                _tempCpu7.value = array[7]
                _tempCpu8.value = array[8]
                _tempCpu9.value = array[9]
                _tempCpu10.value = array[10]
                _tempCpu11.value = array[11]
                _tempCpu12.value = array[12]
                _tempCpu13.value = array[13]
                _tempCpu14.value = array[14]
                _tempCpu15.value = array[15]
                _tempCpu16.value = array[16]
                _tempCpu17.value = array[17]
                _tempCpu18.value = array[18]
                _tempCpu19.value = array[19]

                val temps = Temps(
                    timeStamp,
                    array[0],
                    array[1],
                    array[2],
                    array[3],
                    array[4],
                    array[5],
                    array[6],
                    array[7],
                    array[8],
                    array[9],
                    array[10],
                    array[11],
                    array[12],
                    array[13],
                    array[14],
                    array[15],
                    array[16],
                    array[17],
                    array[18],
                    array[19],
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
        _message.value = "Database cleared"
    }

    private fun getTempBat(): String {
        val intent = registerReceiver
        val tempBat = (intent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            0
        ))?.div(10).toString()
        return "BAT $tempBat"
    }

    private fun getTempsCpu(): List<String> {
        val listAddresses = arrayOf(
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
            "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
            "/sys/devices/platform/tegra_tmon/temp1_input",
            "/sys/kernel/debug/tegra_thermal/temp_tj",
            "/sys/devices/platform/s5p-tmu/temperature",
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/class/hwmon/hwmon0/device/temp1_input",
            "/sys/devices/virtual/thermal/thermal_zone1/temp",
            "/sys/devices/platform/s5p-tmu/curr_temp",
            "/sys/class/thermal/thermal_zone17/temp",
            "/sys/class/thermal/thermal_zone18/temp",
            "/sys/class/thermal/thermal_zone19/temp",
            "/sys/class/thermal/thermal_zone20/temp",
            "/sys/class/thermal/thermal_zone21/temp",
            "/sys/class/thermal/thermal_zone22/temp"
        )
        val tempCpu = mutableListOf<String>()
        var reader: BufferedReader? = null
        for (item in listAddresses.indices) {
            try {
                reader =
                    BufferedReader(FileReader(listAddresses[item]))
                val line = reader.readLine().toFloat()
                val result = if (line > 10000) line / 1000
                else if (line > 1000) line / 100
                else if (line > 100) line / 10
                else line
                if (result > 0) {
                    tempCpu.add(item, result.toString())
                } else {
                    tempCpu.add(item, "not found")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                tempCpu.add(item, "not found")
            } finally {
                reader?.close()
            }
        }
        return tempCpu
    }

    private fun getTempCpu(): List<String> {
        val tempCpu = mutableListOf<String>()
        var temp: String?
        var type: String?
        var count = 0
        var iteration = 0
        do {
            temp = getTemp(iteration)
            type = getType(iteration)
            if (temp != null && type != null && temp.toFloat() > 0) {
                val result = "$type $temp"
                tempCpu.add(count, result)
                count++
                iteration++
            } else {
                iteration++
            }
        } while (count < MAX_COUNT_TEMP_REGISTERS)

        return tempCpu
    }

    private fun getTemp(step: Int): String? {
        val process: Process = Runtime.getRuntime().exec(
            "cat sys/class/thermal/thermal_zone$step/temp"
        )
        val reader =
            BufferedReader(InputStreamReader(process.inputStream))
        return try {
            process.waitFor()
            val line = reader.readLine().toFloat() / 1000.0f
            line.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            reader.close()
            process.destroy()
        }
    }

    private fun getType(step: Int): String? {
        val process: Process = Runtime.getRuntime().exec(
            "cat sys/class/thermal/thermal_zone$step/type"
        )
        val reader =
            BufferedReader(InputStreamReader(process.inputStream))
        return try {
            process.waitFor()
            reader.readLine()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            reader.close()
            process.destroy()
        }
    }

    fun saveCsv() {
        cycleWriteData = false
        viewModelScope.launch {
            _temps = getAllTemps.invoke()
            kotlin.runCatching {
                val filePath = getFilePath()
                val csvFail = File( filePath,"temperatures.csv")
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
                    _message.value = "The csv file is saved in CSV_FILE directory"
                } catch (e: Exception) {
                    _message.value = "Writing csv file failed"
                } finally {
                    outputStreamWriter.close()
                    fileOutputStream.close()
                    cycleWriteData = true
                }
            }
        }
    }

    private fun getFilePath(): File {
        val filePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
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
            val tempCpu0 = temps[item].tempCpu0
            val tempCpu1 = temps[item].tempCpu1
            val tempCpu2 = temps[item].tempCpu2
            val tempCpu3 = temps[item].tempCpu3
            val tempCpu4 = temps[item].tempCpu4
            val tempCpu5 = temps[item].tempCpu5
            val tempCpu6 = temps[item].tempCpu6
            val tempCpu7 = temps[item].tempCpu7
            val tempCpu8 = temps[item].tempCpu8
            val tempCpu9 = temps[item].tempCpu9
            val tempCpu10 = temps[item].tempCpu10
            val tempCpu11 = temps[item].tempCpu11
            val tempCpu12 = temps[item].tempCpu12
            val tempCpu13 = temps[item].tempCpu13
            val tempCpu14 = temps[item].tempCpu14
            val tempCpu15 = temps[item].tempCpu15
            val tempCpu16 = temps[item].tempCpu16
            val tempCpu17 = temps[item].tempCpu17
            val tempCpu18 = temps[item].tempCpu18
            val tempCpu19 = temps[item].tempCpu19
            val tempBat = temps[item].tempBat
            outputStreamWriter.append(
                "$dateTime," +
                        " $tempCpu0," +
                        " $tempCpu1," +
                        " $tempCpu2," +
                        " $tempCpu3," +
                        " $tempCpu4," +
                        " $tempCpu5," +
                        " $tempCpu6," +
                        " $tempCpu7, " +
                        "$tempCpu8," +
                        " $tempCpu9," +
                        " $tempCpu10," +
                        " $tempCpu11," +
                        " $tempCpu12," +
                        " $tempCpu13," +
                        " $tempCpu14," +
                        " $tempCpu15," +
                        " $tempCpu16, " +
                        "$tempCpu17," +
                        " $tempCpu18, " +
                        "$tempCpu19," +
                        " $tempBat\n"
            )
        }
    }

        companion object {
            private const val INTERVAL = 1000L
            private const val MAX_COUNT_TEMP_REGISTERS = 20
        }
    }