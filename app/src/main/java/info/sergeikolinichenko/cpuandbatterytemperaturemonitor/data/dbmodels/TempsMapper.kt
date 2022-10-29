package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.dbmodels

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:59 (GMT+3) **/

class TempsMapper {

    fun mapEntityToDbModel(temps: Temps) = TempsDbModels(
        timeStamp = temps.timeStamp,
        tempCpu0 = temps.tempCpu0,
        tempCpu1 = temps.tempCpu1,
        tempCpu2 = temps.tempCpu2,
        tempCpu3 = temps.tempCpu3,
        tempCpu4 = temps.tempCpu4,
        tempCpu5 = temps.tempCpu5,
        tempCpu6 = temps.tempCpu6,
        tempCpu7 = temps.tempCpu7,
        tempCpu8 = temps.tempCpu8,
        tempCpu9 = temps.tempCpu9,
        tempCpu10 = temps.tempCpu10,
        tempCpu11 = temps.tempCpu11,
        tempCpu12 = temps.tempCpu12,
        tempCpu13 = temps.tempCpu13,
        tempCpu14 = temps.tempCpu14,
        tempCpu15 = temps.tempCpu15,
        tempCpu16 = temps.tempCpu16,
        tempBat = temps.tempBat
    )

    private fun mapDbModelToEntity(tempsDbModels: TempsDbModels) = Temps(
        timeStamp = tempsDbModels.timeStamp,
        tempCpu0 = tempsDbModels.tempCpu0,
        tempCpu1 = tempsDbModels.tempCpu1,
        tempCpu2 = tempsDbModels.tempCpu2,
        tempCpu3 = tempsDbModels.tempCpu3,
        tempCpu4 = tempsDbModels.tempCpu4,
        tempCpu5 = tempsDbModels.tempCpu5,
        tempCpu6 = tempsDbModels.tempCpu6,
        tempCpu7 = tempsDbModels.tempCpu7,
        tempCpu8 = tempsDbModels.tempCpu8,
        tempCpu9 = tempsDbModels.tempCpu9,
        tempCpu10 = tempsDbModels.tempCpu10,
        tempCpu11 = tempsDbModels.tempCpu11,
        tempCpu12 = tempsDbModels.tempCpu12,
        tempCpu13 = tempsDbModels.tempCpu13,
        tempCpu14 = tempsDbModels.tempCpu14,
        tempCpu15 = tempsDbModels.tempCpu15,
        tempCpu16 = tempsDbModels.tempCpu16,
        tempBat = tempsDbModels.tempBat
    )

    fun mapListDbModelToListEntity(list: List<TempsDbModels>) = list.map {
        mapDbModelToEntity(it)
    }
}