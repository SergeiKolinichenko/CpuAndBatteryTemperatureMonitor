package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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


    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding ?: throw RuntimeException("ActivityMainBinding equal null")

    private var isMonitoring = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkWritePermission()

        initHideOfScroll()
        initSwitch()

        // get extras from ForegroundService
        val extras = intent.extras
        extras?.let {
            if (
                extras.containsKey(START_MONITORING)
                &&
                extras.getLong(START_MONITORING) > 0
            ) {
                viewModel.setTimeStartMonitoring(extras.getLong(START_MONITORING))
            }
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
        viewModel.timeMonitoring.observe(this) {
            if (isMonitoring) {
                binding.tvTimeMonitoring.text =
                    getString(
                        R.string.screen_title_string,
                        viewModel.startMonitoring.getFullDate(),
                        it.differenceInTime()
                    )
            }
        }
        viewModel.cycleForMonitor.observe(this) {
            isMonitoring = it

            binding.swStartStop.isChecked = it

            if (it) lifecycle.addObserver(this)
            else lifecycle.removeObserver(this)
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
            viewModel.saveToFileCsv()
        }
        binding.butExitApp.setOnClickListener {

            ShowSnakebar.showActionSnakebar(
                binding.root,
                binding.bab,
                getString(R.string.would_like_exit_app),
                getString(R.string.exit_application),
                ::exitApp
            )
        }
        binding.swStartStop.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMonitorMode(isChecked)
            if (!isChecked) {
                with(binding) {
                    tvTimeMonitoring.text = getString(
                        R.string.monitoring_stopped
                    )
                }
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

    private fun exitApp() {
        lifecycle.removeObserver(this)
        finish()
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
        startIntent.putExtra(START_MONITORING, viewModel.startMonitoring)
        startService(startIntent)
    }

    override fun onStart(owner: LifecycleOwner) {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        stopIntent.putExtra(START_MONITORING, START_MONITORING_ERROR)
        startService(stopIntent)
    }

    private fun checkWritePermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE &&
            permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.saveToFileCsv()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_WRITE_STORAGE_SDK_FROM_Q) {
            val contentResolver: ContentResolver = contentResolver

            val takeFlags = data?.flags?.and(
                (Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            ) ?: throw RuntimeException("Not available folderUri")

            //uri каталога, в который будет разрешена запись
            val folderUri: Uri = data.data ?: throw RuntimeException("Not available folderUri")

            contentResolver.takePersistableUriPermission(folderUri, takeFlags)
            val pickedDir = DocumentFile.fromTreeUri(this, folderUri)
            viewModel.saveFileFromQEnd(pickedDir, contentResolver)
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
        const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101
        const val REQUEST_CODE_WRITE_STORAGE_SDK_FROM_Q = 111
        const val END_OF_LINE = "\n"
        const val SPACE = " "
        private const val STRING_OF_TITLE = "title_string"
        private const val STRING_OF_TEMP = "temperature"
        private const val BATTERY = "Battery"
        private const val NOT_FOUND = -1
    }
}