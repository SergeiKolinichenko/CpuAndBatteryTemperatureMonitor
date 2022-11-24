package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.ForegroundService
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainActivity

/** Created by Sergei Kolinichenko on 24.11.2022 at 18:51 (GMT+3) **/

@ApplicationScope
@Component(modules = [DataModule::class, ViewModelsModule::class])
interface ApplicationComponent {

    fun inject(activity: MainActivity)
    fun inject(service: ForegroundService)


    @Component.Factory
    interface Factory{
        fun create(@BindsInstance application: Application): ApplicationComponent
    }
}