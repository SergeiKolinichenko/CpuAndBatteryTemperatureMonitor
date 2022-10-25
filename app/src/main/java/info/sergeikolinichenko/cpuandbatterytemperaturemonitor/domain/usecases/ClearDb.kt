package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository

/** Created by Sergei Kolinichenko on 25.10.2022 at 10:45 (GMT+3) **/

class ClearDb(private val repository: TempMonRepository) {

    suspend operator fun invoke() {
        repository.clearDb()
    }
}