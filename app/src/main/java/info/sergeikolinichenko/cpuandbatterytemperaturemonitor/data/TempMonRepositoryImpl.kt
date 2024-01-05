package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.database.TempsDao
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.dbmodels.TempsMapper
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:52 (GMT+3) **/

class TempMonRepositoryImpl @Inject constructor(
    private val dao: TempsDao,
    private val preferences: SharedPreferences,
    private val mapper: TempsMapper
) : TempMonRepository {

    override suspend fun clearDb() {
        dao.clearDatabase()
    }

    override suspend fun addTemps(temps: Temps) {
        dao.addTemps(mapper.mapEntityToDbModel(temps))
    }

    override suspend fun getAllTemps(): List<Temps> {
        return mapper.mapListDbModelToListEntity(dao.getTemps())
    }

    override fun getAllTempsLivedata(): LiveData<List<Temps>> {
        val listOfTemps: LiveData<List<Temps>> =
            dao.getTempsLiveData().map {
                mapper.mapListDbModelToListEntity(it)
            }
        return listOfTemps
    }

    override fun setStateMonitoring(mode: Boolean) {
        preferences.edit().putBoolean(MODE_MONITORING, mode).apply()
    }

    override fun getStateMonitoring() = preferences.getBoolean(MODE_MONITORING, false)

    override fun setTimeStartMonitoring(timeStamp: Long) {
        preferences.edit().putLong(TIME_START_MONITORING, timeStamp).apply()
    }

    override fun getTimeStartMonitoring() = preferences.getLong(TIME_START_MONITORING, -1)

    companion object {
        private const val MODE_MONITORING = "MODE_MONITORING"
        private const val TIME_START_MONITORING = "TIME_START_MONITORING"
    }

}