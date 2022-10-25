package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data

import android.app.Application
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.database.AppDatabase
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.dbmodels.TempsMapper
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:52 (GMT+3) **/

class TempMonRepositoryImpl(application: Application): TempMonRepository {

    private val dao = AppDatabase.getInstance(application).tempsDao()
    private val mapper = TempsMapper()

    override suspend fun clearDb() {
        dao.clearDatabase()
    }

    override suspend fun addTemps(temps: Temps) {
        dao.addTemps(mapper.mapEntityToDbModel(temps))
    }

    override suspend fun getAllTemps(): List<Temps> {
        return mapper.mapListDbModelToListEntity(dao.getTemperatures())
    }

//    override fun getAllTemps(): LiveData<List<Temps>> {
//        val list: LiveData<List<Temps>> = Transformations.map(
//            dao.getTemperatures()) {
//                mapper.mapListDbModelToListEntity(it)
//            }
//        return list
//    }

}