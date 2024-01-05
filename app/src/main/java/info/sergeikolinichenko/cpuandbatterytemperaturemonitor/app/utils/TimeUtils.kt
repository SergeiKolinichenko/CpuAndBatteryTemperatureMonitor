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

    fun Long.differenceInTime(): String {
        val seconds = this / 1000 % 60
        val minutes = this / 1000 / 60 % 60
        val hours = this / 1000 / 60 / 60 % 24
        val days = this / 1000 / 60 / 60 / 24
        return String.format( "%d %02d:%02d:%02d", days, hours, minutes, seconds )
    }

}