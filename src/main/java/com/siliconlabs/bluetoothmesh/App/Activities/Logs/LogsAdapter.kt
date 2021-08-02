package com.siliconlabs.bluetoothmesh.App.Activities.Logs

import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.logs_adapter.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LogsAdapter(context: Context, private val logs: List<File>) : BaseAdapter() {
    private val layoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: layoutInflater.inflate(R.layout.logs_adapter, parent, false)
        return view.apply {
            val file = getItem(position)
            val modifiedDate = Date(file.lastModified())
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            tv_name.text = file.nameWithoutExtension
            tv_size.text = Formatter.formatFileSize(context, file.length())
            tv_modified.text = dateFormat.format(modifiedDate)
        }
    }

    override fun getItem(position: Int): File = logs[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = logs.size
}