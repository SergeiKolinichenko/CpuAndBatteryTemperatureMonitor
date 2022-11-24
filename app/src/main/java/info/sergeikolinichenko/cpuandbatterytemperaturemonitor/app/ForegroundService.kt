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
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.TimeUtils.differenceInTime
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.TimeUtils.getFullDate
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.AddTemps
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.FileReader
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 25.10.2022 at 10:52 (GMT+3) **/

class ForegroundService : Service() {

    private var isServiceStarted = false
    private var startMonitoring: Long = START_MONITORING_ERROR
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null

    @Inject
    lateinit var addTemps: AddTemps

    private val component by lazy {
        (application as TempsApp).component
    }

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
        component.inject(this)
        super.onCreate()

        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as?
                    NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            if (intent.hasExtra(START_MONITOR) && startMonitoring == START_MONITORING_ERROR) {
                startMonitoring = intent.getLongExtra(
                    START_MONITOR,
                    START_MONITORING_ERROR
                )
            }
        }

        processCommand(intent)

        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
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
                val tempCpu = getTempCpu()
                val elapsedTime = (timeStamp - startMonitoring).differenceInTime()
                val content = getString(
                    R.string.screen_title_string,
                    startMonitoring.getFullDate(),
                    elapsedTime
                )

                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(content)
                )

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

    private fun getTempBat(): String {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = registerReceiver(null, intentFilter)
        val tempBat = (intent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            0
        ))?.div(10).toString()
        return "BAT $tempBat"
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
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP //Intent.FLAG_ACTIVITY_NEW_TASK
        resultIntent.putExtra(START_MONITOR, startMonitoring)
        return PendingIntent.getActivity(
            this,
            REQUEST_CODE_START_MONITORING,
            resultIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L

        const val INVALID = "INVALID"
        const val COMMAND_START = "COMMAND_START"
        const val COMMAND_STOP = "COMMAND_STOP"
        const val COMMAND_ID = "COMMAND_ID"
        const val START_MONITOR = "START_MONITORING"
        const val START_MONITORING_ERROR = -1L
        const val REQUEST_CODE_START_MONITORING = 222

        const val NUMBER_OF_DATA_READ_CYCLES = 100
        const val STRING_SEPARATOR = ", "
        const val ITEM_SEPARATOR = ":"
    }
}