package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:43 (GMT+3) **/

data class Temps(
    val timeStamp: Long,
    val tempCpu: String,
    val tempBat: String
)
