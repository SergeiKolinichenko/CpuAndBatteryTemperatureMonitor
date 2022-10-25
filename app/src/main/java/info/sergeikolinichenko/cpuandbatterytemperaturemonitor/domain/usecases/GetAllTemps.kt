package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:39 (GMT+3) **/

class GetAllTemps(private val repository: TempMonRepository) {

    suspend operator fun invoke(): List<Temps> {
        return repository.getAllTemps()
    }
}