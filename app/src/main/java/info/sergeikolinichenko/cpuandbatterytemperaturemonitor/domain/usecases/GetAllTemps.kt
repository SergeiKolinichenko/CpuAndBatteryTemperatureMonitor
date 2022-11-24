package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:39 (GMT+3) **/

class GetAllTemps @Inject constructor(private val repository: TempMonRepository) {

    suspend operator fun invoke(): List<Temps> {
        return repository.getAllTemps()
    }
}