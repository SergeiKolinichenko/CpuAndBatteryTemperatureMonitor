package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils

import java.text.SimpleDateFormat
import java.util.Locale

/** Created by Sergei Kolinichenko on 25.10.2022 at 10:34 (GMT+3) **/

object TimeUtils {

    fun Long.getTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return timeFormat.format(this)
    }

    fun Long.getFullDate(): String {
        val fullDateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
        return fullDateFormat.format(this)
    }


}