package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.R
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.COMMAND_ID
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.COMMAND_START
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.COMMAND_STOP
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.ITEM_SEPARATOR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.START_MONITORING
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.START_MONITORING_ERROR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.STRING_SEPARATOR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.ShowSnakebar
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.TimeUtils.differenceInTime
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.TimeUtils.getFullDate
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.databinding.ActivityMainBinding
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps


class MainActivity : AppCompatActivity(), DefaultLifecycleObserver {

    private val viewModelFactory by lazy { MainViewModelFactory(application) }
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            viewModelFactory
        )[MainViewModel::class.java]
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var isMonitoring = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(binding.root)

        initHideOfScroll()
        initSwitch()

        if (isMyServiceRunning(ForegroundService::class.java)) {
            viewModel.getStartMonitoringTime()
        } else {
            getStartTimeMonitoring(intent) // get extras from ForegroundService
        }

        // Observers
        viewModel.tempsLiveData.observe(this) {
            binding.tvTitle.text = getSplitString(it, STRING_OF_TITLE)
            binding.tvTemp.text = getSplitString(it, STRING_OF_TEMP)
        }
        viewModel.intent.observe(this) {
            startActivityForResult(it, REQUEST_CODE_WRITE_STORAGE_SDK_FROM_Q)
        }
        viewModel.message.observe(this) {
            ShowSnakebar.showSnakebar(binding.root, binding.bab, getString(it))
        }
        viewModel.monitoringDuration.observe(this) {
            if (isMonitoring) {
                binding.tvTimeMonitoring.text =
                    getString(
                        R.string.screen_title_string,
                        viewModel.monitoringStartTime.getFullDate(),
                        it.differenceInTime()
                    )
            }
        }
        viewModel.onOffMonitoringCycle.observe(this) {
            isMonitoring = it

            if (it) lifecycle.addObserver(this)
            else lifecycle.removeObserver(this)
        }
        viewModel.monitoringStatusLiveData.observe(this) {
            binding.swStartStop.isChecked = it
        }

        // OnClickListeners
        binding.butClearDb.setOnClickListener {

            ShowSnakebar.showActionSnakebar(
                binding.root,
                binding.bab,
                getString(R.string.would_like_clear_database),
                getString(R.string.clear_database),
                ::clearDatabase
            )
        }
        binding.butSaveFile.setOnClickListener {
            viewModel.saveFileStart()
        }
        binding.swStartStop.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMonitorMode(isChecked)
            if (!isChecked) {
                binding.tvTimeMonitoring.text = getString(
                    R.string.monitoring_stopped
                )
            }
        }
    }

    private fun getStartTimeMonitoring(intent: Intent) {
        val extras = intent.extras
        extras?.let {
            if (
                extras.containsKey(START_MONITORING)
                &&
                extras.getLong(START_MONITORING) > 0
            ) {
                viewModel.setStartMonitoringTime(extras.getLong(START_MONITORING))
            }
        }
    }

    private fun initSwitch() {
        binding.swStartStop.thumbTintList =
            ContextCompat.getColorStateList(this, R.color.thumb_selector)
        binding.swStartStop.trackTintList =
            ContextCompat.getColorStateList(this, R.color.track_selector)
    }

    private fun clearDatabase() {
        viewModel.clearDatabase()
    }

    private fun getSplitString(list: List<Temps>, kind: String): String {
        val resultString = StringBuilder()
        val lastIndex = list.lastIndex

        if (lastIndex >= 0) {
            val lastItemList = list[lastIndex].tempCpu
                .replace(ITEM_SEPARATOR, SPACE)
                .split(STRING_SEPARATOR)
                .toList()

            for (item in lastItemList) {
                val index = item.indexOf(SPACE)
                if (index != NOT_FOUND) {
                    when (kind) {
                        STRING_OF_TITLE -> resultString.append(item.substring(0, index))
                        STRING_OF_TEMP -> resultString.append(item.substring(index + 1))
                    }
                    resultString.append(END_OF_LINE)
                }
            }
            when (kind) {
                STRING_OF_TITLE -> resultString.append(BATTERY)
                STRING_OF_TEMP -> resultString.append(list[lastIndex].tempBat)
            }
        }
        return resultString.toString()
    }

    override fun onStop(owner: LifecycleOwner) {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(START_MONITORING, viewModel.monitoringStartTime)
        startService(startIntent)
    }

    override fun onStart(owner: LifecycleOwner) {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        stopIntent.putExtra(START_MONITORING, START_MONITORING_ERROR)
        startService(stopIntent)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name.equals(service.service.className)) {
                return true
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_WRITE_STORAGE_SDK_FROM_Q) {
            val contentResolver: ContentResolver = contentResolver

            val takeFlags = data?.flags?.and(
                (Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            ) ?: throw RuntimeException("Not available folderUri")

            //uri of the directory that will be writable
            val folderUri: Uri = data.data ?: throw RuntimeException("Not available folderUri")

            contentResolver.takePersistableUriPermission(folderUri, takeFlags)
            val pickedDir = DocumentFile.fromTreeUri(this, folderUri)
            viewModel.saveFileEnd(pickedDir, contentResolver)
        }
    }

    private fun initHideOfScroll() {
        if (binding.bab.isScrolledDown) {
            binding.bab.performShow(true)
        } else {
            binding.bab.performHide(true)
        }
    }

    companion object {
        const val REQUEST_CODE_WRITE_STORAGE_SDK_FROM_Q = 111
        const val END_OF_LINE = "\n"
        const val SPACE = " "
        private const val STRING_OF_TITLE = "title_string"
        private const val STRING_OF_TEMP = "temperature"
        private const val BATTERY = "Battery"
        private const val NOT_FOUND = -1
    }
}