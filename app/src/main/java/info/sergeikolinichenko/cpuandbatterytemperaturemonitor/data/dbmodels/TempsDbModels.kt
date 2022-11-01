package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.dbmodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:56 (GMT+3) **/

@Entity(tableName = "temperatures")
data class TempsDbModels(
    @PrimaryKey(autoGenerate = false)
    val timeStamp: Long,
    val tempCpu: String,
    val tempBat: String
)
