package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:38 (GMT+3) **/

class StopServes(private val repository: TempMonRepository) {

    operator fun invoke() {
        repository.stopServes()
    }
}