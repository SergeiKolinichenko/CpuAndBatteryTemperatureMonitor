package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.dbmodels

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:59 (GMT+3) **/

class TempsMapper @Inject constructor(){

    fun mapEntityToDbModel(temps: Temps) = TempsDbModels(
        timeStamp = temps.timeStamp,
        tempCpu = temps.tempCpu,
        tempBat = temps.tempBat
    )

    private fun mapDbModelToEntity(tempsDbModels: TempsDbModels) = Temps(
        timeStamp = tempsDbModels.timeStamp,
        tempCpu = tempsDbModels.tempCpu,
        tempBat = tempsDbModels.tempBat
    )

    fun mapListDbModelToListEntity(list: List<TempsDbModels>) = list.map {
        mapDbModelToEntity(it)
    }
}