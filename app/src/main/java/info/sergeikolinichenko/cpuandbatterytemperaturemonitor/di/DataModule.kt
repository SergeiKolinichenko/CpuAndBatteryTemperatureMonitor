package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.di

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.TempMonRepositoryImpl
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.database.AppDatabase
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.database.TempsDao
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.preferences.TempsPreferences
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.TempMonRepository
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain.usecases.AddTemps

/** Created by Sergei Kolinichenko on 24.11.2022 at 18:32 (GMT+3) **/

@Module
interface DataModule {

    @Binds
    @ApplicationScope
    fun bindTempMonRepository(impl: TempMonRepositoryImpl): TempMonRepository

    companion object{

        @Provides
        @ApplicationScope
        fun provideTempsDao(application: Application): TempsDao {
            return AppDatabase.getInstance(application).tempsDao()
        }

        @Provides
        @ApplicationScope
        fun provideSharedPreferences(application: Application): SharedPreferences {
            return TempsPreferences.getInstance(application)
        }

        @Provides
        fun provideRegisterReceiver(application: Application): Intent? {
            return application.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        }
    }
}