/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Network

import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlabs.bluetoothmesh.App.Activities.Main.MainActivity
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.GroupList.GroupListFragment
import com.siliconlabs.bluetoothmesh.App.Utils.ErrorMessageConverter
import com.siliconlabs.bluetoothmesh.App.Views.MeshToast
import com.siliconlabs.bluetoothmesh.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.network_fragment_screen.*
import javax.inject.Inject


class NetworkFragment : DaggerFragment(), NetworkView {
    private lateinit var rotate: Animation
    private var meshStatusBtn: ImageView? = null
    private var meshIconStatus = NetworkView.MeshIconState.DISCONNECTED

    private var pageAdapter: NetworkPageAdapter? = null

    @Inject
    lateinit var networkPresenter: NetworkPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rotate = AnimationUtils.loadAnimation(context, R.anim.rotate)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        networkPresenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        networkPresenter.onPause()
        meshStatusBtn?.clearAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkPresenter.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.network_fragment_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        pageAdapter = NetworkPageAdapter(childFragmentManager, context!!)
        view_pager.adapter = pageAdapter

        tab_layout.setupWithViewPager(view_pager)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_groups_toolbar, menu)

        val menuIcon = menu.findItem(R.id.proxy_menu)

        meshStatusBtn?.clearAnimation()
        meshStatusBtn?.visibility = View.INVISIBLE
        meshStatusBtn?.setOnClickListener(null)

        meshStatusBtn = menuIcon?.actionView as ImageView

        setMeshIconState(meshIconStatus)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.proxy_menu) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    // View

    override fun showFragment(fragmentName: NetworkView.FragmentName) {
        view_pager.currentItem = fragmentName.ordinal
    }

    override fun setActionBarTitle(title: String) {
        (activity as MainActivity).setActionBar(title)
    }

    override fun setMeshIconState(iconState: NetworkView.MeshIconState) {
        activity?.runOnUiThread {
            meshIconStatus = iconState

            meshStatusBtn?.apply {
                when (iconState) {
                    NetworkView.MeshIconState.DISCONNECTED -> {
                        setImageResource(R.drawable.ic_mesh_red)
                        clearAnimation()
                    }
                    NetworkView.MeshIconState.CONNECTING -> {
                        setImageResource(R.drawable.ic_mesh_yellow)
                        startAnimation(rotate)
                    }
                    NetworkView.MeshIconState.CONNECTED -> {
                        setImageResource(R.drawable.ic_mesh_green)
                        clearAnimation()
                    }
                }

                setOnClickListener {
                    networkPresenter.meshIconClicked(iconState)
                }
            }
        }
    }

    override fun showToast(toastMessage: NetworkView.ToastMessage) {
        activity?.runOnUiThread {
            val stringResource = when (toastMessage) {
                NetworkView.ToastMessage.NO_NODE_IN_NETWORK -> R.string.network_fragment_toast_no_node_in_network

                NetworkView.ToastMessage.GATT_NOT_CONNECTED -> R.string.network_fragment_toast_gatt_not_connected
                NetworkView.ToastMessage.GATT_PROXY_DISCONNECTED -> R.string.network_fragment_gatt_proxy_disconnected
                NetworkView.ToastMessage.GATT_ERROR_DISCOVERING_SERVICES -> R.string.network_fragment_gatt_error_discovering_services

                NetworkView.ToastMessage.PROXY_SERVICE_NOT_FOUND -> R.string.network_fragment_toast_no_mesh_proxy_service
                NetworkView.ToastMessage.PROXY_CHARACTERISTIC_NOT_FOUND -> R.string.network_fragment_toast_no_mesh_proxy_characteristic
                NetworkView.ToastMessage.PROXY_DESCRIPTOR_NOT_FOUND -> R.string.network_fragment_toast_no_mesh_proxy_descriptor
            }

            MeshToast.show(requireContext(), stringResource)
        }
    }

    override fun showErrorToast(errorType: ErrorType) {
        activity?.runOnUiThread {
            MeshToast.show(requireContext(), ErrorMessageConverter.convert(requireActivity(), errorType))
        }
    }

    fun refreshFragment(fragmentName: NetworkView.FragmentName) {
        val fragmentToRefresh = pageAdapter?.getFragment(fragmentName.ordinal)
        fragmentToRefresh?.let {
            if (!it.isResumed) {
                return
            }
            when (fragmentName) {
                NetworkView.FragmentName.GROUP_LIST -> {
                    (it as GroupListFragment).onResume()
                }
                NetworkView.FragmentName.DEVICE_LIST -> {
                    (it as DeviceListFragment).onResume()
                }
            }
        }
    }
}