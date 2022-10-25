package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:37 (GMT+3) **/

class GetTemperatures(private val repository: TempMonRepository) {

    operator fun invoke() {
        repository.getTemp()
    }
}