package com.siliconlabs.bluetoothmesh.App.Activities.Logs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.PopupMenu
import com.siliconlabs.bluetoothmesh.App.Views.MeshToast
import com.siliconlabs.bluetoothmesh.R
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_logs.*
import javax.inject.Inject

class LogsActivity : DaggerAppCompatActivity(), LogsActivityView {
    @Inject
    lateinit var presenter: LogsActivityPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)
        title = "Export Logs"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadList()
    }

    private fun loadList() {
        val logs = presenter.getLogs()
        lv_logs.apply {
            adapter = LogsAdapter(this@LogsActivity, logs)
            emptyView = tv_empty_logs
            onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
                presenter.handleItemClicked(logs[position], view)
            }
        }
    }

    override fun openShareOrSavePopup(itemView: View?) {
        PopupMenu(this, itemView, Gravity.END).apply {
            menuInflater.inflate(R.menu.menu_log_actions, menu)
            setOnMenuItemClickListener { item ->
                presenter.handlePopupItemClicked(item)
            }
            show()
        }
    }

    override fun openSaveFileIntent(): Boolean {
        startActivityForResult(presenter.prepareSaveFileIntent(), REQUEST_SAVE_LOGS)
        return true
    }

    override fun openShareFileIntent(): Boolean {
        startActivity(presenter.prepareShareFileIntent(this))
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SAVE_LOGS && resultCode == Activity.RESULT_OK) {
            if (!presenter.handleSaveFileRequest(data?.data, contentResolver)) {
                MeshToast.show(this, getString(R.string.logs_save_error))
            }
        }
    }

    companion object {
        private const val REQUEST_SAVE_LOGS = 1
    }
}