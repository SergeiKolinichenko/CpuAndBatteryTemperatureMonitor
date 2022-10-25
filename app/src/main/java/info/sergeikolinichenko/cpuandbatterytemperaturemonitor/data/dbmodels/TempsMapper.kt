package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.dbmodels

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:59 (GMT+3) **/

class TempsMapper {

    fun mapEntityToDbModel(temps: Temps) = TempsDbModels(
        timeStamp = temps.timeStamp,
        tempCpu = temps.tempCpu,
        tempBat = temps.tempBat
    )

    fun mapDbModelToEntity(tempsDbModels: TempsDbModels) = Temps(
        timeStamp = tempsDbModels.timeStamp,
        tempCpu = tempsDbModels.tempCpu,
        tempBat = tempsDbModels.tempBat
    )
}