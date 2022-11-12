package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils

import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

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
        return String.format(
            "%d %02d:%02d:%02d",
            this / 1000 / 3600 / 24,
            this / 1000 / 3600,
            this / 1000 / 60 % 60,
            this / 1000 % 60
        )
    }
}