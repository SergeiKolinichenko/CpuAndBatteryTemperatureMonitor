package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.domain

/** Created by Sergei Kolinichenko on 25.10.2022 at 07:41 (GMT+3) **/

interface TempMonRepository {

    fun getTemp()
    fun startServes()
    fun stopServes()
    fun saveDataCsv()
}