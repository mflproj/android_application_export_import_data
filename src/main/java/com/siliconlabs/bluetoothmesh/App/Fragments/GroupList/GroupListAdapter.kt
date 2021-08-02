/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.GroupList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.siliconlab.bluetoothmesh.adk.data_model.group.Group
import com.siliconlabs.bluetoothmesh.App.Views.SwipeBaseAdapter
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.groups_adapter.view.*
import java.util.*

class GroupListAdapter(val context: Context, private val groupItemListener: GroupItemListener) :
        SwipeBaseAdapter<Group>(GroupInfoComparator()) {

    override fun generateView(position: Int, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.groups_adapter, parent, false)
    }

    override fun fillValues(position: Int, convertView: View?) {
        val group = getItem(position)

        convertView?.apply {
            tv_group_name.text = group.name
            tv_group_devices.text = context.getString(R.string.group_adapter_devices_label).format(group.nodes.size)
            tv_group_key_index.text = group.appKey.keyIndex.toString()

            iv_config.setOnClickListener {
                groupItemListener.onEditClickListener(group)
            }
            iv_remove.setOnClickListener {
                groupItemListener.onDeleteClickListener(group)
            }
            swipe.apply {
                surfaceView.setOnLongClickListener {
                    convertView.swipe.open()
                    return@setOnLongClickListener true
                }
                surfaceView.setOnClickListener {
                    groupItemListener.onGroupClickListener(group)
                }
                showMode = SwipeLayout.ShowMode.LayDown
                addDrag(SwipeLayout.DragEdge.Right, convertView.swipe_menu)
            }
        }
    }

    interface GroupItemListener {
        fun onDeleteClickListener(groupInfo: Group)
        fun onEditClickListener(groupInfo: Group)
        fun onGroupClickListener(groupInfo: Group)
    }

    // Comparator

    class GroupInfoComparator : Comparator<Group> {
        override fun compare(o1: Group, o2: Group): Int {
            return o1.appKey.keyIndex.compareTo(o2.appKey.keyIndex)
        }
    }
}