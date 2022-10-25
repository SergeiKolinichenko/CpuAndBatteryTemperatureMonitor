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
import android.util.Log
import androidx.core.app.NotificationCompat
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.R
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainActivity
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
                val tempCpu = getTempCpu()
                val tempBat = getTempBat()

                val content = String.format("CPU: %d BAT: %d", tempCpu.toInt(), tempBat.toInt())
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(content)
                )

                val temps = Temps(
                    timeStamp, tempCpu, tempBat
                )
                addTemps.invoke(temps)

                delay(INTERVAL)
            }
        }
    }

    private fun getTempBat(): Float {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = registerReceiver(null, intentFilter)
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
    }
}