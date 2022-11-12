package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain

import androidx.lifecycle.LiveData
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:41 (GMT+3) **/

interface TempMonRepository {

    suspend fun clearDb()
    suspend fun addTemps(temps: Temps)
    suspend fun getAllTemps(): List<Temps>
    fun getAllTempsLivedata(): LiveData<List<Temps>>
    fun setStartStopMonitor(mode: Boolean)
    fun getStartStopMonitor(): Boolean
}