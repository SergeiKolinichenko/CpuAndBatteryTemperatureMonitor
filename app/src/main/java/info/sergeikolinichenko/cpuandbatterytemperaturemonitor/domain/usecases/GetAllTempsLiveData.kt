package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases

import androidx.lifecycle.LiveData
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.models.Temps

/** Created by Sergei Kolinichenko on 31.10.2022 at 20:16 (GMT+3) **/

class GetAllTempsLiveData(private val repository: TempMonRepository) {

    operator fun invoke(): LiveData<List<Temps>> {
        return repository.getAllTempsLivedata()
    }
}