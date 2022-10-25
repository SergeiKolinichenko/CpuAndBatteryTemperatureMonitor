package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils

import java.text.SimpleDateFormat
import java.util.*

/** Created by Sergei Kolinichenko on 25.10.2022 at 10:34 (GMT+3) **/

object Utils {
    const val INVALID = "INVALID"
    const val COMMAND_START = "COMMAND_START"
    const val COMMAND_STOP = "COMMAND_STOP"
    const val COMMAND_ID = "COMMAND_ID"

    fun Long.getTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return timeFormat.format(this)
    }

    fun Long.getFullDate(): String {
        val fullDateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
        return fullDateFormat.format(this)
    }
}