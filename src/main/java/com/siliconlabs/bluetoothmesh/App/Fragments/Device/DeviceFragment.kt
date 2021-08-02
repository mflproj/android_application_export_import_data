/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlabs.bluetoothmesh.App.Activities.Main.MainActivity
import com.siliconlabs.bluetoothmesh.App.Utils.ErrorMessageConverter
import com.siliconlabs.bluetoothmesh.App.Views.CustomAlertDialogBuilder
import com.siliconlabs.bluetoothmesh.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.device_screen.*
import kotlinx.android.synthetic.main.dialog_loading.*
import javax.inject.Inject

class DeviceFragment : DaggerFragment(), DeviceView {
    private val TAG: String = javaClass.canonicalName!!

    @Inject
    lateinit var devicePresenter: DevicePresenter

    private var loadingDialog: AlertDialog? = null

    private var firstConfig: Boolean = false

    companion object {
        const val FIRST_CONFIG = "FIRST_CONFIG"

        fun newInstance(firstConfig: Boolean): DeviceFragment {
            val args = Bundle()
            args.putBoolean(FIRST_CONFIG, firstConfig)

            val fragment = DeviceFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(FIRST_CONFIG)) {
                firstConfig = it.getBoolean(FIRST_CONFIG)

                devicePresenter.firstConfig = firstConfig
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.device_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        view_pager.adapter = DevicePageAdapter(childFragmentManager, context!!)

        tab_layout.setupWithViewPager(view_pager)
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        devicePresenter.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        devicePresenter.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        devicePresenter.onDestroy()
    }

    override fun setActionBarTitle(title: String) {
        (activity as MainActivity).setActionBar(title)
    }

    override fun showLoadingDialog() {
        activity?.runOnUiThread {
            loadingDialog?.apply {
                if (isShowing) {
                    return@runOnUiThread
                }
            }

            val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null)
            val builder = CustomAlertDialogBuilder(activity!!, R.style.AppTheme_Light_Dialog_Alert_Wrap)
            builder.apply {
                setView(view)
                setCancelable(false)
                setPositiveButton(activity!!.getString(R.string.dialog_positive_ok)) { dialog, _ ->
                    dialog.dismiss()
                    activity?.supportFragmentManager?.popBackStack()
                }
            }

            loadingDialog = builder.create()
            loadingDialog?.apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                show()

                getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.GONE
            }
        }
    }

    override fun setLoadingDialogMessage(message: String, showCloseButton: Boolean, closeFragmentOnClick: Boolean) {
        activity?.runOnUiThread {
            loadingDialog?.apply {
                if (!isShowing) {
                    return@runOnUiThread
                }
                loading_text.text = message

                if (showCloseButton) {
                    loading_icon.visibility = View.GONE
                    getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.VISIBLE
                }
                if (closeFragmentOnClick) {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        dismiss()
                        activity?.supportFragmentManager?.popBackStack()
                    }
                }
            }
        }
    }

    override fun setLoadingDialogMessage(errorType: ErrorType?, showCloseButton: Boolean) {
        setLoadingDialogMessage(ErrorMessageConverter.convert(activity!!, errorType!!), showCloseButton)
    }

    override fun setLoadingDialogMessage(loadingMessage: DeviceView.LOADING_DIALOG_MESSAGE, showCloseButton: Boolean, closeFragmentOnClick: Boolean) {
        val activity = activity!!

        when (loadingMessage) {
            DeviceView.LOADING_DIALOG_MESSAGE.CONFIG_CONNECTING -> setLoadingDialogMessage(activity.getString(R.string.device_config_connecting), showCloseButton)
            DeviceView.LOADING_DIALOG_MESSAGE.CONFIG_DISCONNECTED -> setLoadingDialogMessage(activity.getString(R.string.device_config_disconnected), showCloseButton)
        }
    }

    override fun dismissLoadingDialog() {
        activity?.runOnUiThread {
            loadingDialog?.apply {
                dismiss()
                loadingDialog = null
            }
        }
    }
}