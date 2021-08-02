/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.MainFragment

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.siliconlabs.bluetoothmesh.App.Activities.Logs.LogsActivity
import com.siliconlabs.bluetoothmesh.App.Activities.Main.MainActivity
import com.siliconlabs.bluetoothmesh.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.dialog_about.view.*
import kotlinx.android.synthetic.main.dialog_export_keys.view.*
import kotlinx.android.synthetic.main.main_screen.*
import javax.inject.Inject
import com.siliconlab.bluetoothmesh.adk.BuildConfig as AdkBuildConfig
import com.siliconlabs.bluetoothmesh.BuildConfig as AppBuildConfig


class MainFragment : DaggerFragment(), MainFragmentView {

    private val className = javaClass.canonicalName!!

    private val coarseLocationRequestCode = 1
    private val saveKeysIntentCode = 1000

    private var showedLocationAlertDialog = false
    private var requestedLocationPermission = false

    private var locationManager: LocationManager? = null

    @Inject
    lateinit var mainFragmentPresenter: MainFragmentPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as MainActivity).setActionBar()
        mainFragmentPresenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        mainFragmentPresenter.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_main_screen_toolbar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about_bluetooth_mesh -> {
                showAboutDialog()
                return true
            }
            R.id.credits -> {
                showCreditsDialog()
                return true
            }
            R.id.export_keys -> {
                showExportKeysDialog()
                return true
            }
            R.id.export_logs -> {
                openLogsActivity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openLogsActivity() {
        val intent = Intent(context, LogsActivity::class.java)
        startActivity(intent)
    }

    // View
    override fun setView() {
        setTabLayout()

        checkBTAdapter()
        checkGPS()

        setEnablingButtons()
    }

    private fun showAboutDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_about, null).apply {
            val appVersionName = StringBuilder(getString(R.string.dialog_about_app_version).format(AppBuildConfig.VERSION_NAME))
                    .apply {
                        if (AppBuildConfig.DEBUG) {
                            AppBuildConfig.GIT_SHA.takeIf { it.isNotEmpty() }?.let { appendln().append("Git SHA ").append(it) }
                            AppBuildConfig.BUILD_NUMBER?.let { appendln().append("build ").append(it) }
                        }
                    }
            val adkVersionName = getString(R.string.dialog_about_adk_version).format(AdkBuildConfig.ADK_VERSION)
            val apiVersionName = StringBuilder(getString(R.string.dialog_about_api_version).format(AdkBuildConfig.VERSION_NAME))
                    .apply {
                        if (AppBuildConfig.DEBUG) {
                            AdkBuildConfig.BUILD_NUMBER?.let { appendln().append("build ").append(it) }
                        }
                    }

            tv_app_version.text = appVersionName
            tv_adk_version.text = adkVersionName
            tv_api_version.text = apiVersionName
        }

        AlertDialog.Builder(requireContext(), R.style.AppTheme_Light_Dialog_Alert_Wrap)
                .setView(view)
                .setPositiveButton(R.string.dialog_positive_ok) { dialog, _ ->
                    dialog.dismiss()
                }.show()
    }

    private fun showCreditsDialog() {
        val licences = StringBuilder(getString(R.string.network_menu_credits_dagger_license))
                .append("\n\n\n")
                .append(getString(R.string.network_menu_credits_gson_license))
                .append("\n\n\n")
                .append(getString(R.string.network_menu_credits_rxjava_license))
                .append("\n\n\n")
                .append(getString(R.string.network_menu_credits_swipe_layout_license))

        AlertDialog.Builder(requireContext())
                .setTitle(R.string.network_menu_credits)
                .setMessage(licences)
                .setPositiveButton(R.string.dialog_positive_ok, null)
                .show()
    }


    private fun showExportKeysDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_export_keys, null).apply {
            btn_share_keys.setOnClickListener {
                mainFragmentPresenter.shareNetworkKeys()
            }

            btn_save_keys.setOnClickListener {
                mainFragmentPresenter.saveNetworkKeys()
            }
        }

        AlertDialog.Builder(requireContext(), R.style.AppTheme_Light_Dialog_Alert_Wrap)
                .setTitle(getString(R.string.network_menu_export_cryptographic_keys))
                .setMessage("\n" + getString(R.string.network_menu_export_cryptographic_keys_message))
                .setView(view)
                .create()
                .show()
    }

    // Permission callback

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            coarseLocationRequestCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (showedLocationAlertDialog) {
                        return
                    }
                    showedLocationAlertDialog = true

                    AlertDialog.Builder(requireContext())
                            .setCancelable(false)
                            .setTitle(getString(R.string.main_activity_dialog_location_permission_not_granted_title))
                            .setMessage(getString(R.string.main_activity_dialog_location_permission_not_granted_message))
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                showedLocationAlertDialog = false
                            }.show()
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(className, "coarse location permission granted")

                    setView()
                }
            }
        }
    }

//

    private fun setTabLayout() {
        view_pager.adapter = MainFragmentPageAdapter(childFragmentManager, context!!)
        tab_layout.setupWithViewPager(view_pager)
    }

    override fun setEnablingButtons() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            bluetooth_enable.visibility = View.VISIBLE
            bluetooth_enable_btn.setOnClickListener {
                BluetoothAdapter.getDefaultAdapter().enable()
            }
        } else {
            bluetooth_enable.visibility = View.GONE
        }

        if (!isLocationEnabled()) {
            location_enable.visibility = View.VISIBLE
            location_enable_btn.setOnClickListener {
                location_enable.visibility = View.GONE
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                if (Build.VERSION.SDK_INT >= 23)
                    requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            coarseLocationRequestCode
                    )
            }
        } else {
            location_enable.visibility = View.GONE
        }
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager?.let {
            it.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || it.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } ?: false
    }

    private fun checkGPS() {
        if (requestedLocationPermission) {
            return
        }
        requestedLocationPermission = true

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    coarseLocationRequestCode
            )
        }
    }

    private fun checkBTAdapter() {
        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            AlertDialog.Builder(requireContext())
                    .setCancelable(false)
                    .setTitle(getString(R.string.main_activity_dialog_not_support_ble_title))
                    .setMessage(getString(R.string.main_activity_dialog_not_support_ble_message))
                    .setPositiveButton(getString(R.string.main_activity_dialog_not_support_ble_positive_button)) { _, _ ->
                        activity!!.finish()
                    }
                    .show()
        }
    }

    override fun showShareKeysIntent(intent: Intent) {
        activity?.startActivity(intent)
    }

    override fun showSaveKeysIntent(intent: Intent) {
        startActivityForResult(intent, saveKeysIntentCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK && requestCode == saveKeysIntentCode) {
            mainFragmentPresenter.saveKeysToPhoneStorage(intent!!)
        }
    }


}