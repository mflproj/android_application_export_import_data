package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler

import android.content.Context
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.SchedulerAction.*
import com.siliconlabs.bluetoothmesh.R

class SchedulerParams(val index: Int, val year: Int, val months: Set<Month>, val day: Int, val hour: Int, val minute: Int, val second: Int, val daysOfWeek: Set<DayOfWeek>, val action: Action, val transitionTime: Int, val scene: Int) {

    fun validate(context: Context): String {
        if (!isYearValid(year)) return yearInvalidRangeMessage(context)
        if (!isDayValid(day)) return dayInvalidRangeMessage(context)
        if (!isHourValid(hour)) return hourInvalidRangeMessage(context)
        if (!isMinuteValid(minute)) return minuteInvalidRangeMessage(context)
        if (!isSecondValid(second)) return secondInvalidRangeMessage(context)
        return ""
    }

    private fun isYearValid(year: Int): Boolean {
        return year in YEAR_MIN..Year.EVERY_YEAR.value
    }

    private fun isDayValid(day: Int): Boolean {
        return day in Day.EVERY_DAY.value..DAY_MAX
    }

    private fun isHourValid(hour: Int): Boolean {
        return hour in HOUR_MIN..Hour.ANY_HOUR.value
    }

    private fun isMinuteValid(minute: Int): Boolean {
        return minute in MINUTE_MIN..Minute.ANY_MINUTE.value
    }

    private fun isSecondValid(second: Int): Boolean {
        return second in SECOND_MIN..Second.ANY_SECOND.value
    }

    companion object {
        //values do not include extra values like EVERY_DAY.value
        const val YEAR_MIN = 0
        const val YEAR_MAX = 99
        const val DAY_MIN = 1
        const val DAY_MAX = 31
        const val HOUR_MIN = 0
        const val HOUR_MAX = 23
        const val MINUTE_MIN = 0
        const val MINUTE_MAX = 59
        const val SECOND_MIN = 0
        const val SECOND_MAX = 59

        fun yearInvalidRangeMessage(context: Context): String {
            return context.getString(R.string.device_adapter_scheduler_invalid_range, context.getString(R.string.device_adapter_scheduler_year), YEAR_MIN, YEAR_MAX)
        }

        fun dayInvalidRangeMessage(context: Context): String {
            return context.getString(R.string.device_adapter_scheduler_invalid_range, context.getString(R.string.device_adapter_scheduler_day), DAY_MIN, DAY_MAX)
        }

        fun hourInvalidRangeMessage(context: Context): String {
            return context.getString(R.string.device_adapter_scheduler_invalid_range, context.getString(R.string.device_adapter_scheduler_hour), HOUR_MIN, HOUR_MAX)
        }

        fun minuteInvalidRangeMessage(context: Context): String {
            return context.getString(R.string.device_adapter_scheduler_invalid_range, context.getString(R.string.device_adapter_scheduler_minute), MINUTE_MIN, MINUTE_MAX)
        }

        fun secondInvalidRangeMessage(context: Context): String {
            return context.getString(R.string.device_adapter_scheduler_invalid_range, context.getString(R.string.device_adapter_scheduler_second), SECOND_MIN, SECOND_MAX)
        }
    }
}