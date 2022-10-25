package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.COMMAND_ID
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.COMMAND_START
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.COMMAND_STOP
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils.Utils.getTime
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), DefaultLifecycleObserver {

    private val viewModelFactory by lazy { MainViewModelFactory(application) }
    private val viewModel by lazy { ViewModelProvider(
        this,
        viewModelFactory
    )[MainViewModel::class.java] }

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
    get() = _binding ?: throw RuntimeException("ActivityMainBinding equal null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycle.addObserver(this)

        // Observers
        viewModel.timeStamp.observe(this){
            binding.tvTime.text = it.getTime()
        }
        viewModel.tempCpu.observe(this){
            val mesTempCpu = String.format("Temperature CPU: %d", it.toInt())
            binding.tvTempCpu.text = mesTempCpu
        }
        viewModel.tempBat.observe(this){
            val mesTempBat = String.format("Temperature Bat: %d", it.toInt())
            binding.tvTempBattery.text = mesTempBat
        }

        // OnClickListeners
        binding.butClearDb.setOnClickListener {
            viewModel.clearDatabase()
        }
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()
        _binding = null
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("MyLog", "onStop")
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startService(startIntent)
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d("MyLog", "onStart")
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}