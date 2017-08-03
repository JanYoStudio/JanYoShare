package com.janyo.janyoshare.util

import android.content.Context
import android.content.SharedPreferences

import com.janyo.janyoshare.R

class Settings(private val context: Context)
{
	private val sharedPreferences: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

	var isAutoClean: Boolean
		get() = sharedPreferences.getBoolean(context.getString(R.string.key_auto_clean), true)
		set(isAutoClean) = sharedPreferences.edit().putBoolean(context.getString(R.string.key_auto_clean), isAutoClean).apply()

	var sort: Int
		get() = sharedPreferences.getInt("sortType", 0)
		set(type) = sharedPreferences.edit().putInt("sortType", type).apply()

	var isFirst: Boolean
		get() = sharedPreferences.getBoolean("isFirst", true)
		set(isFirst) = sharedPreferences.edit().putBoolean("isFirst", isFirst).apply()

	var isDeveloperModeEnable: Boolean
		get() = sharedPreferences.getBoolean("isDeveloperModeEnable", false)
		set(isDeveloperModeEnable)
		{
			if (!isDeveloperModeEnable)
			{
				isAutoUploadLog = false
			}
			sharedPreferences.edit().putBoolean("isDeveloperModeEnable", isDeveloperModeEnable).apply()
		}

	var isAutoUploadLog: Boolean
		get() = sharedPreferences.getBoolean("isAutoUploadLog", false)
		set(isAutoUploadLog) = sharedPreferences.edit().putBoolean("isAutoUploadLog", isAutoUploadLog).apply()

	var excludeList: Set<String>
		get() = sharedPreferences.getStringSet("excludeList", emptySet())
		set(excludeList) = sharedPreferences.edit().putStringSet("excludeList", excludeList).apply()

	var excludeNameList: Set<String>
		get() = sharedPreferences.getStringSet("excludeNameList", emptySet())
		set(excludeNameList) = sharedPreferences.edit().putStringSet("excludeNameList", excludeNameList).apply()

	var excludeSize: Float
		get() = sharedPreferences.getFloat("excludeSize", 0f)
		set(excludeSize) = sharedPreferences.edit().putFloat("excludeSize", excludeSize).apply()

	var excludeRegularExpression: String
		get() = sharedPreferences.getString("excludeRegularExpression", "")
		set(excludeRegularExpression) = sharedPreferences.edit().putString("excludeRegularExpression", excludeRegularExpression).apply()
}
