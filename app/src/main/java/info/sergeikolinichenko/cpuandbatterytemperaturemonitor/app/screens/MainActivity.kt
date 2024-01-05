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
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.START_MONITOR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.START_MONITORING_ERROR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.STRING_SEPARATOR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.TempsApp
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainViewModel.Companion.ACTIVITY_FOR_RESULT_FAIL
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainViewModel.Companion.ACTIVITY_FOR_RESULT_OK
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainViewModel.Companion.END_OF_LINE
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainViewModel.Companion.SPACE
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.ShowSnakebar
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.TimeUtils.differenceInTime
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.TimeUtils.getFullDate
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.databinding.ActivityMainBinding
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import javax.inject.Inject


class MainActivity : AppCompatActivity(), DefaultLifecycleObserver {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModelFactory: MainViewModelFactory
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            viewModelFactory
        )[MainViewModel::class.java]
    }

    private val component by lazy {
        (application as TempsApp).component
    }

    private var monitorOn = true

    override fun onCreate(savedInstanceState: Bundle?) {

        component.inject(this)
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(binding.root)

        initHideOfScroll()
        initSwitch()
        initObserveApp()
        initOnClickListenersApp()

        if (isMyServiceRunning(ForegroundService::class.java)) {
            viewModel.getStartMonitorTime()
        } else {
            getStartTimeMonitoring(intent) // get extras from ForegroundService
        }
    }

    private fun getStartTimeMonitoring(intent: Intent) {
        val extras = intent.extras
        extras?.let {
            if (
                extras.containsKey(START_MONITOR)
                &&
                extras.getLong(START_MONITOR) > 0
            ) {
                viewModel.setStartMonitorTime(extras.getLong(START_MONITOR))
            }
        }
    }

    // Observers
    private fun initObserveApp() {
        viewModel.tempsLiveData.observe(this) {
            binding.tvTitle.text = getSplitString(it, STRING_OF_TITLE)

            if (monitorOn) {
                binding.tvTemp.text = getSplitString(it, STRING_OF_TEMP)
            }
        }
        viewModel.intent.observe(this) {
            startActivityForResult(it, REQUEST_CODE_WRITE_STORAGE_SDK_FROM_Q)
        }
        viewModel.message.observe(this) {
            ShowSnakebar.showSnakebar(binding.root, binding.bab, getString(it))
        }
        viewModel.monitorDuration.observe(this) {
            if (monitorOn) {
                binding.tvTimeMonitoring.text =
                    getString(
                        R.string.screen_title_string,
                        viewModel.monitorStartTime.getFullDate(),
                        it.differenceInTime()
                    )
            }
        }
        viewModel.monitorCycleOnOff.observe(this) {
            monitorOn = it

            if (!monitorOn) {
                binding.tvTimeMonitoring.text = getString(
                    R.string.monitor_stopped
                )
                binding.tvTemp.text = ""
            }

            if (it) lifecycle.addObserver(this)
            else lifecycle.removeObserver(this)
        }
        viewModel.monitorStatStartLD.observe(this) {
            binding.swStartStop.isChecked = it
        }
        viewModel.errorApp.observe(this) {
            if (it) {
                binding.tvTitle.text = getString(R.string.monitiring_possible_not_possible)
                lifecycle.removeObserver(this)
            }
        }
    }

    // OnClickListeners
    private fun initOnClickListenersApp() {
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
        startIntent.putExtra(START_MONITOR, viewModel.monitorStartTime)
        startService(startIntent)
    }

    override fun onStart(owner: LifecycleOwner) {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        stopIntent.putExtra(START_MONITOR, START_MONITORING_ERROR)
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

        if (requestCode == REQUEST_CODE_WRITE_STORAGE_SDK_FROM_Q && data != null) {
            val contentResolver: ContentResolver = contentResolver

            val takeFlags = data.flags.and(
                (Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            )

            //uri of the directory that will be writable
            val folderUri: Uri = data.data ?: throw RuntimeException("Not available folderUri")

            contentResolver.takePersistableUriPermission(folderUri, takeFlags)
            val pickedDir = DocumentFile.fromTreeUri(this, folderUri)
            viewModel.saveFileEnd(pickedDir, contentResolver, ACTIVITY_FOR_RESULT_OK)
        } else viewModel.saveFileEnd(null, null, ACTIVITY_FOR_RESULT_FAIL)
    }

    private fun initHideOfScroll() {
        if (binding.bab.isScrolledDown) {
            binding.bab.performShow(true)
        } else {
            binding.bab.performHide(true)
        }
    }

    companion object {
        private const val REQUEST_CODE_WRITE_STORAGE_SDK_FROM_Q = 111
        private const val STRING_OF_TITLE = "title_string"
        private const val STRING_OF_TEMP = "temperature"
        private const val BATTERY = "Battery"
        private const val NOT_FOUND = -1
    }
}