@file:Suppress("DEPRECATION")

package com.janyo.janyoshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.SwitchPreference
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
		checkUpdate = findPreference(getString(R.string.key_check_update))

		auto_clean!!.isChecked = settings!!.isAutoClean
		if (settings!!.isAutoClean)
		{
			auto_clean!!.summary = "已开启自动清理，将在下次启动时清理临时文件"
		}
		else
		{
			auto_clean!!.summary = "已关闭自动清理"
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
						.setPositiveButton("开启") { _, _ -> settings!!.isAutoClean = true }
						.setNegativeButton("取消") { _, _ ->
							auto_clean!!.isChecked = false
							settings!!.isAutoClean = false
						}
						.setOnDismissListener {
							auto_clean!!.isChecked = settings!!.isAutoClean
							if (settings!!.isAutoClean)
							{
								auto_clean!!.summary = "已开启自动清理，将在下次启动时清理临时文件"
							}
							else
							{
								auto_clean!!.summary = "已关闭自动清理"
							}
						}
						.show()
			}
			else
			{
				settings!!.isAutoClean = false
				auto_clean!!.summary = "已关闭自动清理"
			}
			true
		}
		about!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			AlertDialog.Builder(this@SettingsActivity)
					.setTitle(" ")
					.setView(LayoutInflater.from(this@SettingsActivity).inflate(R.layout.dialog_about, LinearLayout(this@SettingsActivity), false))
					.setPositiveButton("关闭", null)
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
					.setPositiveButton("确定", null)
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