package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository

/** Created by Sergei Kolinichenko on 17.11.2022 at 18:12 (GMT+3) **/

class SetTimeStartMonitoring(private val repository: TempMonRepository) {

    operator fun invoke(timeStamp: Long) {
        repository.setTimeStartMonitoring(timeStamp)
    }
}