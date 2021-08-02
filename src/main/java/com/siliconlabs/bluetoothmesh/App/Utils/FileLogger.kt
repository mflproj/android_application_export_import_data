/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Utils

import android.content.Context
import android.content.pm.ApplicationInfo
import java.io.File
import java.io.IOException
import java.util.*

object FileLogger {

    private const val maxLogsCount = 10
    private const val maxLogLifetime = 1000L * 60 * 60 * 24 * 3  // 3 days

    var directory: File? = null
        private set

    fun setup(context: Context, appInfo: ApplicationInfo) {
        directory = File(context.filesDir, "logs")
        directory!!.run {
            if (exists()) {
                cleanup()
            }
            if (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                if (!exists()) {
                    mkdirs()
                }

                startLogging()
            }
        }
    }

    private fun File.cleanup() {
        removeOldLogs()
        removeOverflowingLogs()
    }

    private fun File.removeOldLogs() {
        listFiles()?.filter { System.currentTimeMillis() - it.lastModified() > maxLogLifetime }
                ?.forEach { it.delete() }
    }

    private fun File.removeOverflowingLogs() {
        listFiles()?.takeIf { it.size >= maxLogsCount }
                ?.sortedByDescending { it.lastModified() }
                ?.drop(maxLogsCount - 1)
                ?.forEach { it.delete() }
    }

    private fun startLogging() {
        val filePath = String.format("%s/logcat_%s.txt", directory?.absolutePath, Calendar.getInstance().time.toString())
        try {
            Runtime.getRuntime().exec(arrayOf("logcat", "-v", "time", "-f", filePath))
        } catch (e: IOException) {
        }
    }
}