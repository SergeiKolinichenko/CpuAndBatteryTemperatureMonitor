package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.TempMonRepositoryImpl
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.AddTemps
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.ClearDb

/** Created by Sergei Kolinichenko on 25.10.2022 at 09:04 (GMT+3) **/

class MainViewModelFactory(application: Application): ViewModelProvider.Factory {

    private val repository = TempMonRepositoryImpl(application)
    private val clearDb = ClearDb(repository)
    private val addTemps = AddTemps(repository)
    private val registerReceiver = application.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            MainViewModel(
                registerReceiver,
                clearDb,
                addTemps
            ) as T
        } else throw RuntimeException("Unknown view Model class $modelClass")
    }
}