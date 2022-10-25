package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.dbmodels.TempsDbModels

/** Created by Sergei Kolinichenko on 25.10.2022 at 08:07 (GMT+3) **/

@Dao
interface TempsDao {

    @Query("SELECT * FROM temperatures")
    suspend fun getTemperatures(): List<TempsDbModels>

    @Query("DELETE FROM temperatures")
    suspend fun clearDatabase()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTemps(tempsDbModels: TempsDbModels)
}