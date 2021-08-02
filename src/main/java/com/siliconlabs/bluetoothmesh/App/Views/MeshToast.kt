/*
 * Copyright © 2020 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Views

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.siliconlabs.bluetoothmesh.R

class MeshToast {

    companion object {

        fun show(context: Context, text: CharSequence) {
            Toast(context)
                    .apply { view = prepareToastView(context, text) }
                    .show()
        }

        fun show(context: Context, @StringRes resId: Int) {
            show(context, context.getString(resId))
        }

        private fun prepareToastView(context: Context, text: CharSequence): View? {
            return View.inflate(context, R.layout.toast, null).apply {
                val toastTextView = findViewById<TextView>(R.id.toast_text)
                toastTextView.text = text
            }
        }
    }
}