package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.R
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainActivity
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainViewModel
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.COMMAND_ID
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.COMMAND_START
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.COMMAND_STOP
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.INVALID
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.TempMonRepositoryImpl
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.AddTemps
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

/** Created by Sergei Kolinichenko on 25.10.2022 at 10:52 (GMT+3) **/

class ForegroundService: Service() {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null
    private val repository by lazy { TempMonRepositoryImpl(application)}
    private val addTemps by lazy { AddTemps(repository) }

    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Temperature monitor")
            .setGroup("Temperature")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.thermometer)
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as?
                    NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun processCommand(intent: Intent?) {
        when(intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                commandStart()
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    private fun commandStart() {
        if (isServiceStarted) {
            return
        }
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer()
        } finally {
            isServiceStarted = true
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun continueTimer() {
        job = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                val timeStamp = System.currentTimeMillis()
                val tempBat = getTempBat()
                val array = getTempCpu()

                val content = "BAT $tempBat CPU @${array[0]} @${array[1]} @${array[2]} @${array[3]} @${array[4]}}"//String.format("CPU: %d BAT: %s", tempCpu.toInt(), tempBat)
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(content)
                )

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

    private fun getTempBat(): String {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = registerReceiver(null, intentFilter)
        val tempBat = (intent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            0
        ))?.div(10).toString()
        return "BAT $tempBat"
    }

    private fun getTempCpu(): List<String> {
        val tempCpu  = mutableListOf<String>()
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
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
        finally {
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
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
        finally {
            reader.close()
            process.destroy()
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(
            this,
            MainActivity::class.java
        )
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {

        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
        private const val MAX_COUNT_TEMP_REGISTERS = 20
    }
}