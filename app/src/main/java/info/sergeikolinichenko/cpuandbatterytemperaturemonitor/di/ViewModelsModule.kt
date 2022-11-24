package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.screens.MainViewModel

/** Created by Sergei Kolinichenko on 24.11.2022 at 19:22 (GMT+3) **/

@Module
interface ViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModule(viewModel: MainViewModel): ViewModel
}