@file:Suppress("DEPRECATION")

package com.janyo.janyoshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.SwitchPreference
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import com.janyo.janyoshare.util.Settings

class SettingsActivity : PreferenceActivity()
{
	private var settings: Settings? = null
	private var toolbar: Toolbar? = null
	private var auto_clean: SwitchPreference? = null
	private var about: Preference? = null
	private var howToUse: Preference? = null
	private var openSourceAddress: Preference? = null
	private var license: Preference? = null
	private var checkUpdate: Preference? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		settings = Settings(this@SettingsActivity)
		addPreferencesFromResource(R.xml.preferences)
		initialization()
		monitor()
		toolbar!!.title = title
	}

	private fun initialization()
	{
		auto_clean = findPreference(getString(R.string.key_auto_clean)) as SwitchPreference
		about = findPreference(getString(R.string.key_about))
		howToUse = findPreference(getString(R.string.key_how_to_use))
		openSourceAddress = findPreference(getString(R.string.key_open_source_address))
		license = findPreference(getString(R.string.key_license))
		checkUpdate = findPreference(getString(R.string.key_check_update))

		auto_clean!!.isChecked = settings!!.isAutoClean
		if (settings!!.isAutoClean)
		{
			auto_clean!!.setSummary(R.string.summary_auto_clean_on)
		}
		else
		{
			auto_clean!!.setSummary(R.string.summary_auto_clean_off)
		}
	}

	private fun monitor()
	{
		auto_clean!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
			val isAutoClean = !auto_clean!!.isChecked
			if (isAutoClean)
			{
				AlertDialog.Builder(this@SettingsActivity)
						.setTitle(" ")
						.setMessage(R.string.autoCleanWarn)
						.setPositiveButton(R.string.action_open) { _, _ -> settings!!.isAutoClean = true }
						.setNegativeButton(R.string.action_cancel) { _, _ ->
							auto_clean!!.isChecked = false
							settings!!.isAutoClean = false
						}
						.setOnDismissListener {
							auto_clean!!.isChecked = settings!!.isAutoClean
							if (settings!!.isAutoClean)
							{
								auto_clean!!.setSummary(R.string.summary_auto_clean_on)
							}
							else
							{
								auto_clean!!.setSummary(R.string.summary_auto_clean_off)
							}
						}
						.show()
			}
			else
			{
				settings!!.isAutoClean = false
				auto_clean!!.setSummary(R.string.summary_auto_clean_off)
			}
			true
		}
		about!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			AlertDialog.Builder(this@SettingsActivity)
					.setTitle(" ")
					.setView(LayoutInflater.from(this@SettingsActivity).inflate(R.layout.dialog_about, LinearLayout(this@SettingsActivity), false))
					.setPositiveButton(R.string.action_close, null)
					.show()
			false
		}
		howToUse!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val view = LayoutInflater.from(this@SettingsActivity).inflate(R.layout.dialog_help, LinearLayout(this@SettingsActivity), false)
			val textView = view.findViewById<TextView>(R.id.autoCleanWarn)
			if (settings!!.isAutoClean)
			{
				textView.visibility = View.VISIBLE
			}
			AlertDialog.Builder(this@SettingsActivity)
					.setTitle(" ")
					.setView(view)
					.setPositiveButton(R.string.action_done, null)
					.show()
			false
		}
		openSourceAddress!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val intent = Intent()
			intent.action = "android.intent.action.VIEW"
			val content_url = Uri.parse(getString(R.string.address_open_source))
			intent.data = content_url
			startActivity(intent)
			false
		}
		license!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val view_license = LayoutInflater.from(this@SettingsActivity).inflate(R.layout.dialog_license, NestedScrollView(this@SettingsActivity), false)
			val text_license_point1 = view_license.findViewById<TextView>(R.id.license_point1)
			val text_license_point2 = view_license.findViewById<TextView>(R.id.license_point2)
			val text_license_point3 = view_license.findViewById<TextView>(R.id.license_point3)
			val point = VectorDrawableCompat.create(resources, R.drawable.ic_point, null)
			point!!.setBounds(0, 0, point.minimumWidth, point.minimumHeight)
			text_license_point1.setCompoundDrawables(point, null, null, null)
			text_license_point2.setCompoundDrawables(point, null, null, null)
			text_license_point3.setCompoundDrawables(point, null, null, null)
			AlertDialog.Builder(this)
					.setTitle(" ")
					.setView(view_license)
					.setPositiveButton(R.string.action_done, { _, _ ->
						settings!!.isFirst = false
					})
					.show()
			false
		}
		checkUpdate!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val intent = Intent()
			intent.action = "android.intent.action.VIEW"
			val content_url = Uri.parse(getString(R.string.address_check_update))
			intent.data = content_url
			startActivity(intent)
			false
		}
	}

	override fun setContentView(layoutResID: Int)
	{
		val contentView = LayoutInflater.from(this).inflate(R.layout.activity_settings, LinearLayout(this), false) as ViewGroup
		toolbar = contentView.findViewById<Toolbar>(R.id.toolbar)
		toolbar!!.setNavigationOnClickListener { finish() }

		val contentWrapper = contentView.findViewById<ViewGroup>(R.id.content_wrapper)
		LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true)

		window.setContentView(contentView)
	}
}
