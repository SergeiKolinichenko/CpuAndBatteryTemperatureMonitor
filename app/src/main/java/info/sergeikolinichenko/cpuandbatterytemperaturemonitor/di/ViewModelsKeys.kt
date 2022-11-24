package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.di

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

/** Created by Sergei Kolinichenko on 24.11.2022 at 19:25 (GMT+3) **/

@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)