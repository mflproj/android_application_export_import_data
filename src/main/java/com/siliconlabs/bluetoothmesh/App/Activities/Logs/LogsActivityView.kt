package com.siliconlabs.bluetoothmesh.App.Activities.Logs

import android.view.View

interface LogsActivityView {
    fun openShareOrSavePopup(itemView: View?)
    fun openShareFileIntent(): Boolean
    fun openSaveFileIntent(): Boolean
}
