package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:38 (GMT+3) **/

class AddTemps(private val repository: TempMonRepository) {

    suspend operator fun invoke(temps: Temps) {
        repository.addTemps(temps)
    }
}