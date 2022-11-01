package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.database.AppDatabase
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.dbmodels.TempsMapper
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:52 (GMT+3) **/

class TempMonRepositoryImpl(application: Application) : TempMonRepository {

    private val dao = AppDatabase.getInstance(application).tempsDao()
    private val mapper = TempsMapper()

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
            Transformations.map(dao.getTempsLiveData()) {
                mapper.mapListDbModelToListEntity(it)
            }
        return listOfTemps
    }

}