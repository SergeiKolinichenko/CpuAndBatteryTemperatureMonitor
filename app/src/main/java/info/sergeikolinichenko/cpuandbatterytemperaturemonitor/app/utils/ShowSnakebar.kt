package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils

import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.R

/** Created by Sergei Kolinichenko on 09.11.2022 at 19:19 (GMT+3) **/

object ShowSnakebar {

    fun showSnakebar(
        parentView: View,
        view: View,
        message: String
    ) {
        val icon = R.drawable.information_variant

        val snackBar = Snackbar.make(
            parentView,
            message,
            Snackbar.LENGTH_LONG
        )

        val snackBarView = snackBar.view
        val snackBarText = snackBarView.findViewById<TextView>(
            com.google.android.material.R.id.snackbar_text
        )
        snackBarText.setCompoundDrawablesWithIntrinsicBounds(
            icon, 0, 0, 0
        )
        snackBarText.compoundDrawablePadding = 15
        snackBarText.gravity = Gravity.CENTER
        snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        snackBar.anchorView = view
        snackBar.show()
    }

    fun showActionSnakebar(
        parentView: View,
        view: View,
        message: String,
        signature: String,
        action: () -> Unit
    ) {
        val icon = R.drawable.help

        val snackBar = Snackbar.make(
            parentView,
            message,
            Snackbar.LENGTH_LONG
        )
            .setAction(signature) {
                action()
            }
        val snackBarView = snackBar.view
        val snackBarText = snackBarView.findViewById<TextView>(
            com.google.android.material.R.id.snackbar_text
        )
        snackBarText.setCompoundDrawablesWithIntrinsicBounds(
            icon, 0, 0, 0
        )
        snackBarText.compoundDrawablePadding = 15
        snackBarText.gravity = Gravity.CENTER
        snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        snackBar.anchorView = view
        snackBar.show()
    }
}