/*
 * Copyright © 2020 Silicon Labs, http://www.silabs.com. All rights reserved.
 */
package com.siliconlabs.bluetoothmesh.App.Views

import android.view.View

fun View.makeVisibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}