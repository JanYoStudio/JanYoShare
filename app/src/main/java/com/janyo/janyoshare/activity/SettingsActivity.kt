package com.janyo.janyoshare.activity

import android.os.Bundle
import android.preference.*
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.janyo.janyoshare.R
import com.janyo.janyoshare.fragment.SettingsPreferenceFragment

import com.janyo.janyoshare.util.Settings
import com.mystery0.tools.Logs.Logs

class SettingsActivity : PreferenceActivity()
{
	private val TAG = "SettingsActivity"
	private lateinit var toolbar: Toolbar
	lateinit var coordinatorLayout: CoordinatorLayout

	override fun onCreate(savedInstanceState: Bundle?)
	{
		val settings = Settings.getInstance(this)
		if (settings.dayNight)
			setTheme(R.style.AppTheme_Night_Preference)
		super.onCreate(savedInstanceState)
		fragmentManager.beginTransaction().replace(R.id.content_wrapper, SettingsPreferenceFragment()).commit()
		toolbar.title = title
	}

	override fun setContentView(layoutResID: Int)
	{
		val contentView = LayoutInflater.from(this).inflate(R.layout.activity_settings, LinearLayout(this), false) as ViewGroup
		toolbar = contentView.findViewById(R.id.toolbar)
		toolbar.setNavigationOnClickListener { finish() }

		coordinatorLayout = contentView.findViewById(R.id.coordinatorLayout)

		val contentWrapper = contentView.findViewById<ViewGroup>(R.id.content_wrapper)
		LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true)

		window.setContentView(contentView)
	}

	override fun onDestroy()
	{
		super.onDestroy()
		Logs.i(TAG, "onDestroy: ")
	}
}
