package com.example.newmedisync.utils

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar

object SnackbarUtils {

    fun showTopSnackbar(
        view: View,
        message: String,
        isSuccess: Boolean
    ) {

        val snackbar = Snackbar.make(
            view,
            message,
            Snackbar.LENGTH_SHORT
        )

        val snackbarView = snackbar.view

        val params = snackbarView.layoutParams

        if (params is CoordinatorLayout.LayoutParams) {

            params.gravity = Gravity.TOP
            snackbarView.layoutParams = params
        }

        snackbar.setBackgroundTint(
            if (isSuccess)
                view.context.getColor(android.R.color.holo_green_dark)
            else
                view.context.getColor(android.R.color.holo_red_dark)
        )

        snackbar.show()
    }
}