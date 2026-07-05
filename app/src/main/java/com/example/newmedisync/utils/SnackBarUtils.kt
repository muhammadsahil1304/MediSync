package com.example.newmedisync.utils

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar

object SnackbarUtils {

    fun showTopSnackbar(
        view: View,
        message: String,
        isSuccess: Boolean
    ) {

        if (!view.isAttachedToWindow) return

        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)

        val params = snackbar.view.layoutParams

        when (params) {
            is CoordinatorLayout.LayoutParams -> {
                params.gravity = Gravity.TOP
            }
            is FrameLayout.LayoutParams -> {
                params.gravity = Gravity.TOP
            }
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