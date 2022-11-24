package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases

import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 11.11.2022 at 15:15 (GMT+3) **/

class GetStateMonitoring @Inject constructor(private val repository: TempMonRepository) {

    operator fun invoke() = repository.getStateMonitoring()

}