package com.siliconlabs.bluetoothmesh.App.Activities.Logs

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.siliconlabs.bluetoothmesh.App.Utils.FileLogger
import com.siliconlabs.bluetoothmesh.R
import java.io.File
import java.io.FileNotFoundException

class LogsActivityPresenter(private val view: LogsActivityView) {
    var log: File? = null

    fun getLogs(): List<File> {
        return FileLogger.directory?.listFiles()?.toList()
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
    }

    fun handleItemClicked(item: File, itemView: View) {
        log = item
        view.openShareOrSavePopup(itemView)
    }

    fun handlePopupItemClicked(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_log_actions_share -> view.openShareFileIntent()
            R.id.menu_log_actions_save -> view.openSaveFileIntent()
            else -> false
        }
    }

    fun prepareSaveFileIntent(): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TITLE, log!!.name)
    }

    fun prepareShareFileIntent(context: Context): Intent {
        val logUri = FileProvider.getUriForFile(context, "com.siliconlabs.bluetoothmesh.fileprovider", log!!)
        val sendIntent = Intent()
                .setAction(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_STREAM, logUri)
                .putExtra(Intent.EXTRA_SUBJECT, log!!.nameWithoutExtension)

        return Intent.createChooser(sendIntent, "Share log via").apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addActionsFilter(context, sendIntent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun Intent.addActionsFilter(context: Context, sendIntent: Intent) {
        val resolveInfos = context.packageManager.queryIntentActivities(sendIntent, 0)
        putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, filterExcludedActions(resolveInfos))
    }

    private fun filterExcludedActions(resolveInfos: List<ResolveInfo>): Array<ComponentName> {
        return resolveInfos
                .filter { isActionNameOnExcludedList(it.activityInfo.name) }
                .map { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
                .toTypedArray()
    }

    private fun isActionNameOnExcludedList(actionName: String): Boolean {
        return excludedActionsNames.any { actionName.contains(it) }
    }

    fun handleSaveFileRequest(targetUri: Uri?, contentResolver: ContentResolver): Boolean {
        log!!.inputStream().use { input ->
            val outputStream = try {
                targetUri?.let { contentResolver.openOutputStream(it) } ?: return false
            } catch (e: FileNotFoundException) {
                return false
            }

            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return true
    }

    companion object {
        val excludedActionsNames = arrayOf("SendTextToClipboardActivity", "bluetoothmesh")
    }
}
