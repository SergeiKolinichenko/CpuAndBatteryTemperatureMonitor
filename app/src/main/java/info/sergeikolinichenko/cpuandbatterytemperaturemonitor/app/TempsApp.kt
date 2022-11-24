package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app

import android.app.Application
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.di.DaggerApplicationComponent

/** Created by Sergei Kolinichenko on 24.11.2022 at 19:02 (GMT+3) **/

class TempsApp: Application() {

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}