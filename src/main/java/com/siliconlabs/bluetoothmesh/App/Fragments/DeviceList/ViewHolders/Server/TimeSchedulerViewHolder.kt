/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Server

import android.content.Context
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.SchedulerAction.*
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.response.SchedulerActionStatus
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.DAY_MAX
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.DAY_MIN
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.HOUR_MAX
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.HOUR_MIN
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.MINUTE_MAX
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.MINUTE_MIN
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.SECOND_MAX
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.SECOND_MIN
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.YEAR_MAX
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.YEAR_MIN
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.dayInvalidRangeMessage
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.hourInvalidRangeMessage
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.minuteInvalidRangeMessage
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.secondInvalidRangeMessage
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.SchedulerParams.Companion.yearInvalidRangeMessage
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.DeviceViewHolderBase
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Views.MeshToast
import com.siliconlabs.bluetoothmesh.App.Views.RefreshNodeButton
import com.siliconlabs.bluetoothmesh.App.Views.makeVisibleIf
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.devices_adapter_base_header.view.*
import kotlinx.android.synthetic.main.devices_adapter_base_scene.view.*
import kotlinx.android.synthetic.main.devices_adapter_light_lc.view.swipe
import kotlinx.android.synthetic.main.devices_adapter_scheduler_detail.view.*
import kotlinx.android.synthetic.main.devices_adapter_time_scheduler.view.*

class TimeSchedulerViewHolder(view: View, deviceListLogic: DeviceListAdapterLogic, val context: Context) : DeviceViewHolderBase(view, deviceListLogic) {

    private lateinit var meshNode: MeshNode
    private var selectedEntry = 0

    override fun bindView(meshNode: MeshNode, isNetworkConnected: Boolean) {
        super.bindView(meshNode, isNetworkConnected)
        this.meshNode = meshNode
        view.apply {
            swipe.setup(meshNode)
            scenes_layout.visibility = View.GONE
            iv_device_image.setImageResource(R.drawable.ic_scheduler)

            bindScheduler()
            TimeView(context).initView(view, meshNode, deviceListLogic)

            if (isNetworkConnected) {
                ll_back_control.setOnClickListener {
                    ll_back_control.visibility = View.GONE
                    ll_time_detail.visibility = View.GONE
                    ll_scheduler_detail.visibility = View.GONE
                    ll_time_scheduler_menu.visibility = View.VISIBLE
                }

                ll_scheduler_control.setOnClickListener {
                    ll_time_scheduler_menu.visibility = View.GONE
                    ll_scheduler_detail.visibility = View.VISIBLE
                    ll_back_control.visibility = View.VISIBLE
                }

                ll_time_control.setOnClickListener {
                    ll_time_scheduler_menu.visibility = View.GONE
                    ll_time_detail.visibility = View.VISIBLE
                    ll_back_control.visibility = View.VISIBLE
                }
            } else {
                ll_scheduler_control.setOnClickListener(null)
                ll_time_control.setOnClickListener(null)
                ll_back_control.setOnClickListener(null)
            }

            setEnabledControls(isNetworkConnected)
        }
    }

    private fun View.bindScheduler() {
        setSchedulerVisibilityRules()
        sp_scheduler_entries.adapter = Adapter(context, meshNode.schedules.mapEntries())
        sp_scheduler_entries.setOnItemSelectedListener { index ->
            selectedEntry = index
            bindDefaultAction()
            meshNode.scheduleRegister[index]?.let { bindAction(it) }
        }
        sp_scheduler_entries.setSelection(selectedEntry)
        btn_scheduler_set.setOnClickListener { onSchedulerActionSetButtonClick() }
        iv_scheduler_status_refresh.setOnClickListener {
            deviceListLogic.schedulerLogic.refreshScheduleRegister(meshNode, RefreshNodeListener(it as RefreshNodeButton))
        }
        iv_scheduler_action_refresh.setOnClickListener {
            val index = sp_scheduler_entries.selectedItemPosition
            deviceListLogic.schedulerLogic.refreshSchedulerAction(index, meshNode, RefreshNodeListener(it as RefreshNodeButton))
        }
        setYearEditTextListeners()
    }

    private fun View.setSchedulerVisibilityRules() {
        sp_scheduler_action.setOnItemSelectedListener {
            val actionName = sp_scheduler_action.getItemAtPosition(it).toString()
            sp_scheduler_scene.makeVisibleIf(actionName.contains("Scene"))
        }
        sp_scheduler_hour.setOnItemSelectedListener {
            et_scheduler_specific_hour.makeVisibleIf(it == Hour.values().size)
        }
        sp_scheduler_minute.setOnItemSelectedListener {
            et_scheduler_specific_minute.makeVisibleIf(it == Minute.values().size)
        }
        sp_scheduler_second.setOnItemSelectedListener {
            et_scheduler_specific_second.makeVisibleIf(it == Second.values().size)
        }
        sw_scheduler_every_year.setOnCheckedChangeListener { _, isChecked ->
            et_scheduler_year.isEnabled = isChecked.not()
        }
        sw_scheduler_every_month.setOnCheckedChangeListener { _, isChecked ->
            sp_scheduler_month.isEnabled = isChecked.not()
        }
        sw_scheduler_every_day.setOnCheckedChangeListener { _, isChecked ->
            et_scheduler_day.isEnabled = isChecked.not()
        }
        sw_scheduler_every_day_of_week.setOnCheckedChangeListener { _, isChecked ->
            sp_scheduler_day_of_week.isEnabled = isChecked.not()
        }
    }

    private fun View.onSchedulerActionSetButtonClick() {
        val isSpecificHourSelected = sp_scheduler_hour.selectedItemPosition == Hour.values().size
        val isSpecificMinuteSelected = sp_scheduler_minute.selectedItemPosition == Minute.values().size
        val isSpecificSecondSelected = sp_scheduler_second.selectedItemPosition == Second.values().size

        try {
            val index = sp_scheduler_entries.selectedItemPosition
            val action = Action.values()[sp_scheduler_action.selectedItemPosition]
            val scene = if (action == Action.SCENE_RECALL) sp_scheduler_scene.selectedItemPosition + 1 else 0
            val year = if (sw_scheduler_every_year.isChecked) Year.EVERY_YEAR.value else validateYear(et_scheduler_year.text.substring(1).toInt())
            val months = Month.values().getEveryOrSelected(sw_scheduler_every_month, sp_scheduler_month)
            val day = if (sw_scheduler_every_day.isChecked) Day.EVERY_DAY.value else validateDay(et_scheduler_day.toInt())
            val daysOfWeek = DayOfWeek.values().getEveryOrSelected(sw_scheduler_every_day_of_week, sp_scheduler_day_of_week)
            val hour = if (isSpecificHourSelected) validateHour(et_scheduler_specific_hour.let { if (it.text.isEmpty()) 0 else it.toInt() })
            else Hour.values()[sp_scheduler_hour.selectedItemPosition].value
            val minute = if (isSpecificMinuteSelected) validateMinute(et_scheduler_specific_minute.let { if (it.text.isEmpty()) 0 else it.toInt() })
            else Minute.values()[sp_scheduler_minute.selectedItemPosition].value
            val second = if (isSpecificSecondSelected) validateSecond(et_scheduler_specific_second.let { if (it.text.isEmpty()) 0 else it.toInt() })
            else Second.values()[sp_scheduler_second.selectedItemPosition].value
            val transitionTime = 0
            val schedulerParams = SchedulerParams(index, year, months, day, hour, minute, second, daysOfWeek, action, transitionTime, scene)
            deviceListLogic.schedulerLogic.onSchedulerSetButtonClick(schedulerParams, meshNode, context)
        } catch (e: NumberFormatException) {
            MeshToast.show(context, context.getString(R.string.device_adapter_scheduler_wrong_input_format, e.message))
        } catch (e: InvalidRangeException) {
            MeshToast.show(context, e.toastMessage)
        }
    }

    private fun validateYear(year: Int): Int {
        return if (year in YEAR_MIN..YEAR_MAX) year else throw InvalidRangeException(yearInvalidRangeMessage(context))
    }

    private fun validateDay(day: Int): Int {
        return if (day in DAY_MIN..DAY_MAX) day else throw InvalidRangeException(dayInvalidRangeMessage(context))
    }

    private fun validateHour(hour: Int): Int {
        return if (hour in HOUR_MIN..HOUR_MAX) hour else throw InvalidRangeException(hourInvalidRangeMessage(context))
    }

    private fun validateMinute(minute: Int): Int {
        return if (minute in MINUTE_MIN..MINUTE_MAX) minute else throw InvalidRangeException(minuteInvalidRangeMessage(context))
    }

    private fun validateSecond(second: Int): Int {
        return if (second in SECOND_MIN..SECOND_MAX) second else throw InvalidRangeException(secondInvalidRangeMessage(context))
    }

    private fun View.bindDefaultAction() {
        sp_scheduler_action.setSelection(Action.NO_ACTION.ordinal)
        et_scheduler_year.setText(R.string.device_adapter_scheduler_default_year)
        sp_scheduler_month.setSelection(0)
        et_scheduler_day.setText("")
        sp_scheduler_day_of_week.setSelection(0)
        et_scheduler_year.isEnabled = false
        sp_scheduler_month.isEnabled = false
        et_scheduler_day.isEnabled = false
        sp_scheduler_day_of_week.isEnabled = false
        sw_scheduler_every_year.isChecked = true
        sw_scheduler_every_month.isChecked = true
        sw_scheduler_every_day.isChecked = true
        sw_scheduler_every_day_of_week.isChecked = true
        sp_scheduler_hour.setSelection(0)
        sp_scheduler_minute.setSelection(0)
        sp_scheduler_second.setSelection(0)
    }

    private fun View.bindAction(status: SchedulerActionStatus) {
        sp_scheduler_action.setSelection(status.action.ordinal)
        if (status.action == Action.SCENE_RECALL) {
            sp_scheduler_scene.setSelection(status.scene - 1)
        }
        bindActionDate(status)
        bindActionTime(status)
    }

    private fun View.bindActionDate(status: SchedulerActionStatus) {
        if (status.year == Year.EVERY_YEAR.value) {
            sw_scheduler_every_year.isChecked = true
            et_scheduler_year.isEnabled = false
        } else {
            sw_scheduler_every_year.isChecked = false
            et_scheduler_year.isEnabled = true
            et_scheduler_year.setText(context.getString(R.string.device_adapter_scheduler_year_number_format, status.year))
        }
        if (status.months == Month.values().toSet()) {
            sw_scheduler_every_month.isChecked = true
            sp_scheduler_month.isEnabled = false
        } else {
            sw_scheduler_every_month.isChecked = false
            sp_scheduler_month.isEnabled = true
            status.months.takeUnless { it.isEmpty() }
                    ?.let { sp_scheduler_month.setSelection(it.first().ordinal) }
        }
        if (status.day == Day.EVERY_DAY.value) {
            sw_scheduler_every_day.isChecked = true
            et_scheduler_day.isEnabled = false
        } else {
            sw_scheduler_every_day.isChecked = false
            et_scheduler_day.isEnabled = true
            et_scheduler_day.setText(status.day.toString())
        }
        if (status.daysOfWeek == DayOfWeek.values().toSet()) {
            sw_scheduler_every_day_of_week.isChecked = true
            sp_scheduler_day_of_week.isEnabled = false
        } else {
            sw_scheduler_every_day_of_week.isChecked = false
            sp_scheduler_day_of_week.isEnabled = true
            status.daysOfWeek.takeUnless { it.isEmpty() }
                    ?.let { sp_scheduler_day_of_week.setSelection(it.first().ordinal) }
        }
    }

    private fun View.bindActionTime(status: SchedulerActionStatus) {
        if (status.hour < 24) {
            sp_scheduler_hour.setSelection(Hour.values().size)
            et_scheduler_specific_hour.setText(status.hour.toString())
        } else {
            sp_scheduler_hour.setSelection(status.hour - 24)
        }
        if (status.minute < 60) {
            sp_scheduler_minute.setSelection(Minute.values().size)
            et_scheduler_specific_minute.setText(status.minute.toString())
        } else {
            sp_scheduler_minute.setSelection(status.minute - 60)
        }
        if (status.second < 60) {
            sp_scheduler_second.setSelection(Second.values().size)
            et_scheduler_specific_second.setText(status.second.toString())
        } else {
            sp_scheduler_second.setSelection(status.second - 60)
        }
    }

    private fun View.setYearEditTextListeners() {
        et_scheduler_year.setOnFocusChangeListener { v, hasFocus ->
            Selection.setSelection(et_scheduler_year.text, 1)
        }
        et_scheduler_year.addTextChangedListener(YearTextWatcher(et_scheduler_year))
    }

    /**
     * Preserves edit text format "’YY" and makes it overtype mode and two-sided
     */
    class YearTextWatcher(private val yearEditText: EditText) : TextWatcher {
        private var previousYear: CharSequence = StringBuilder(yearEditText.text)
        private val yearRegex = Regex("’[0-9]{2}")

        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            s?.takeIf { it.matches(yearRegex) && previousYear != s }
                    ?.let { previousYear = StringBuilder(it) }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s == null) {
                restorePreviousYear()
            } else if (s.matches(yearRegex)) {
                return
            } else if (count - before == 1) {
                replaceDigit(s, start)
            } else if (count - before == -1 && start > 0) {
                changeDigitToZero(start)
            } else {
                restorePreviousYear()
            }
        }

        private fun replaceDigit(s: CharSequence, start: Int) {
            val newDigit = s[start].toString()
            val newYear = when {
                start < 2 -> "’$newDigit${s[3]}"
                start == 2 -> "’${s[1]}$newDigit"
                else -> "’${s[2]}$newDigit"
            }
            val selection = when {
                start < 2 -> 2
                else -> 3
            }
            yearEditText.setText(newYear)
            Selection.setSelection(yearEditText.text, selection)
        }

        private fun changeDigitToZero(start: Int) {
            yearEditText.setText(previousYear.replaceRange(start, start + 1, "0"))
            Selection.setSelection(yearEditText.text, start)
        }

        private fun restorePreviousYear() {
            yearEditText.setText(StringBuilder(previousYear))
            Selection.setSelection(yearEditText.text, 1)
        }
    }

    private fun <T> Array<T>.getEveryOrSelected(switch: Switch, spinner: Spinner) =
            if (switch.isChecked) toSet() else setOf(get(spinner.selectedItemPosition))

    private fun BooleanArray.mapEntries() = mapIndexed { index, isScheduled -> "Entry ${index + 1}${if (isScheduled) " (Scheduled)" else ""}" }.toTypedArray()
}

private fun Spinner.setOnItemSelectedListener(onItemSelected: (position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onItemSelected(position)
        }
    }
}

private fun EditText.toInt() = text.toString().toInt()

private class Adapter(context: Context, items: Array<String>) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {
    init {
        setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
    }
}

private class InvalidRangeException(val toastMessage: String) : Exception()