/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.NetworkList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.Subnet
import com.siliconlabs.bluetoothmesh.App.Views.SwipeBaseAdapter
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.networks_adapter.view.*
import java.util.*

class NetworkListAdapter(val context: Context, private val networkItemListener: NetworkItemListener)
    : SwipeBaseAdapter<Subnet>(NetworkInfoComparator()) {

    override fun generateView(position: Int, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.networks_adapter, parent, false)
    }

    override fun fillValues(position: Int, convertView: View?) {
        val networkInfo = getItem(position)

        convertView?.apply {
            tv_group_name.text = networkInfo.name
            tv_group_devices.text = context.getString(R.string.network_adapter_devices_label).format(networkInfo.nodes.size)
            tv_network_groups.text = context.getString(R.string.network_adapter_groups_label).format(networkInfo.groups.size)
            tv_network_key_index.text = networkInfo.netKey.keyIndex.toString()

            iv_config.setOnClickListener {
                networkItemListener.onEditClickListener(networkInfo)
            }
            iv_remove.setOnClickListener {
                networkItemListener.onDeleteClickListener(networkInfo)
            }
            swipe.apply {
                surfaceView.setOnLongClickListener {
                    convertView.swipe.open()
                    return@setOnLongClickListener true
                }
                surfaceView.setOnClickListener {
                    networkItemListener.onNetworkClickListener(networkInfo)
                }
                showMode = SwipeLayout.ShowMode.LayDown
                addDrag(SwipeLayout.DragEdge.Right, convertView.swipe_menu)
            }
        }
    }

    interface NetworkItemListener {
        fun onDeleteClickListener(networkInfo: Subnet)
        fun onEditClickListener(networkInfo: Subnet)
        fun onNetworkClickListener(networkInfo: Subnet)
    }

    // Comparator

    class NetworkInfoComparator : Comparator<Subnet> {
        override fun compare(o1: Subnet, o2: Subnet): Int {
            return o1.netKey.keyIndex.compareTo(o2.netKey.keyIndex)
        }
    }
}