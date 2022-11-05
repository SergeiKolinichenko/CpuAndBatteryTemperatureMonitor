package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.R
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.ITEM_SEPARATOR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService.Companion.STRING_SEPARATOR
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.COMMAND_ID
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.COMMAND_START
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.COMMAND_STOP
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.databinding.ActivityMainBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        lifecycle.addObserver(this)

        checkWritePermission()

        // Observers
        viewModel.tempsLiveData.observe(this) {
            val lastIndex = it.lastIndex
            if (lastIndex >= 0) {
                binding.tvTempBattery.text = getString(R.string.battery, it[lastIndex].tempBat)
                val tempCpu = parseTempCpu(it[lastIndex].tempCpu)
                binding.tvTempCpu.text = tempCpu
            } else {
                binding.tvTempBattery.text = getString(R.string.no_data)
            }
        }

        viewModel.message.observe(this) {
            showToast(getString(it))
        }

        // OnClickListeners
        binding.butClearDb.setOnClickListener {
            showSnakeBar(
                getString(R.string.would_like_clear_database),
                getString(R.string.clear_database),
                ::clearDatabase
            )
        }
        binding.butSaveFile.setOnClickListener {
            viewModel.saveFileCsv()
        }
        binding.butExitApp.setOnClickListener {
            showSnakeBar(
                getString(R.string.would_like_exit_app),
                getString(R.string.exit_application),
                ::exitApp
            )
        }
    }

    private fun clearDatabase() {
        viewModel.clearDatabase()
    }

    private fun exitApp() {
        lifecycle.removeObserver(this)
        finish()
    }

    private fun parseTempCpu(text: String): String {
        return text
            .replace(
            STRING_SEPARATOR,
            END_OF_LINE
        )
            .replace(
                ITEM_SEPARATOR,
                SPACE
            )
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()
        _binding = null
    }

    override fun onStop(owner: LifecycleOwner) {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startService(startIntent)
    }

    override fun onStart(owner: LifecycleOwner) {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
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
                REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE &&
            permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.saveFileCsv()
        }
    }

    private fun showSnakeBar (
        messageText: String,
        buttonText: String,
        action: () -> Unit
    ) {
        Snackbar.make(
            binding.root,
            messageText,
            Snackbar.LENGTH_LONG
        )
            .setAction(buttonText) {action()}
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_CODE = 101
        const val END_OF_LINE = "\n"
        const val SPACE = " "
    }
}