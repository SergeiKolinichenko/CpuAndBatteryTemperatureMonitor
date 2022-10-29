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
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService
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

        lifecycle.addObserver(this)

        checkWritePermission()

        // Observers
        viewModel.tempBat.observe(this) {
            val mesTempBat = String.format("Battery: %s", it)
            binding.tvTempBattery.text = mesTempBat
        }
        viewModel.tempCpu0.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu0.text = mesTempCpu
        }
        viewModel.tempCpu1.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu1.text = mesTempCpu
        }
        viewModel.tempCpu2.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu2.text = mesTempCpu
        }
        viewModel.tempCpu3.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu3.text = mesTempCpu
        }
        viewModel.tempCpu4.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu4.text = mesTempCpu
        }
        viewModel.tempCpu5.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu5.text = mesTempCpu
        }
        viewModel.tempCpu6.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu6.text = mesTempCpu
        }
        viewModel.tempCpu7.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu7.text = mesTempCpu
        }
        viewModel.tempCpu8.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu8.text = mesTempCpu
        }
        viewModel.tempCpu9.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu9.text = mesTempCpu
        }
        viewModel.tempCpu10.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu10.text = mesTempCpu
        }
        viewModel.tempCpu11.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu11.text = mesTempCpu
        }
        viewModel.tempCpu12.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu12.text = mesTempCpu
        }
        viewModel.tempCpu13.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu13.text = mesTempCpu
        }
        viewModel.tempCpu14.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu14.text = mesTempCpu
        }
        viewModel.tempCpu15.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu15.text = mesTempCpu
        }
        viewModel.tempCpu16.observe(this) {
            val mesTempCpu = String.format("CPU: %s", it)
            binding.tvTempCpu16.text = mesTempCpu
        }
        viewModel.message.observe(this) {
            showToast(it)
        }

        // OnClickListeners
        binding.butClearDb.setOnClickListener {
            viewModel.clearDatabase()
        }
        binding.butSaveFile.setOnClickListener {
            viewModel.saveCsv()
        }
        binding.butExitApp.setOnClickListener {
            lifecycle.removeObserver(this)
            finish()
        }
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
            viewModel.saveCsv()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_CODE = 101
    }
}