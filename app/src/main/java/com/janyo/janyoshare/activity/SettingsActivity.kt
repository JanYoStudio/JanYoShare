@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.activity

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.preference.*
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.Volley
import com.janyo.janyoshare.R
import com.janyo.janyoshare.handler.PayHandler
import com.janyo.janyoshare.handler.SettingHandler
import com.janyo.janyoshare.util.AppManager

import com.janyo.janyoshare.util.Settings
import com.mystery0.tools.Logs.Logs
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.collections.HashSet
import kotlin.concurrent.timerTask

class SettingsActivity : PreferenceActivity()
{
	private val TAG = "SettingsActivity"
	private lateinit var settings: Settings
	private lateinit var toolbar: Toolbar
	private lateinit var payHandler: PayHandler
	private lateinit var auto_clean: SwitchPreference
	private lateinit var enableExcludeList: SwitchPreference
	private lateinit var excludeList: Preference
	private lateinit var developerMode: PreferenceCategory
	private lateinit var developerModeEnable: SwitchPreference
	private lateinit var enableExcludeNameList: SwitchPreference
	private lateinit var excludeNameList: Preference
	private lateinit var enableExcludeSize: SwitchPreference
	private lateinit var excludeSize: Preference
	private lateinit var enableExcludeRegularExpression: SwitchPreference
	private lateinit var excludeRegularExpression: Preference
	private lateinit var autoUploadLog: SwitchPreference
	private lateinit var about: Preference
	private lateinit var howToUse: Preference
	private lateinit var openSourceAddress: Preference
	private lateinit var license: Preference
	private lateinit var checkUpdate: Preference
	private lateinit var versionCode: Preference
	private lateinit var support: Preference
	private lateinit var joinTest: Preference
	private lateinit var coordinatorLayout: CoordinatorLayout
	private lateinit var settingHandler: SettingHandler
	private lateinit var progressDialog: ProgressDialog
	private var clickTime = 0
	private var isGooglePlayPay = false

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		settings = Settings(this@SettingsActivity)
		payHandler = PayHandler(this, Volley.newRequestQueue(this))
		addPreferencesFromResource(R.xml.preferences)
		initialization()
		monitor()
		toolbar.title = title
	}

	private fun initialization()
	{
		auto_clean = findPreference(getString(R.string.key_auto_clean)) as SwitchPreference
		enableExcludeList = findPreference(getString(R.string.key_enable_exclude_list)) as SwitchPreference
		excludeList = findPreference(getString(R.string.key_exclude_list))
		developerMode = findPreference(getString(R.string.key_developer_mode)) as PreferenceCategory
		developerModeEnable = findPreference(getString(R.string.key_developer_mode_enable)) as SwitchPreference
		enableExcludeNameList = findPreference(getString(R.string.key_enable_exclude_name)) as SwitchPreference
		excludeNameList = findPreference(getString(R.string.key_exclude_name_list))
		enableExcludeSize = findPreference(getString(R.string.key_enable_exclude_size)) as SwitchPreference
		excludeSize = findPreference(getString(R.string.key_exclude_size))
		enableExcludeRegularExpression = findPreference(getString(R.string.key_enable_exclude_regular_expression)) as SwitchPreference
		excludeRegularExpression = findPreference(getString(R.string.key_exclude_regular_expression))
		autoUploadLog = findPreference(getString(R.string.key_auto_upload_log)) as SwitchPreference
		about = findPreference(getString(R.string.key_about))
		howToUse = findPreference(getString(R.string.key_how_to_use))
		openSourceAddress = findPreference(getString(R.string.key_open_source_address))
		license = findPreference(getString(R.string.key_license))
		checkUpdate = findPreference(getString(R.string.key_check_update))
		versionCode = findPreference(getString(R.string.key_version_code))
		support = findPreference(getString(R.string.key_support))
		joinTest = findPreference(getString(R.string.key_join_test))

		progressDialog = ProgressDialog(this)
		progressDialog.setMessage(getString(R.string.hint_exclude_list_loading))
		progressDialog.setCancelable(false)
		settingHandler = SettingHandler(this, progressDialog, excludeList)

		auto_clean.isChecked = settings.isAutoClean
		developerModeEnable.isChecked = settings.isDeveloperModeEnable
		autoUploadLog.isChecked = settings.isAutoUploadLog
		enableExcludeList.isChecked = settings.excludeList.isNotEmpty()
		enableExcludeNameList.isChecked = settings.excludeNameList.isNotEmpty()
		enableExcludeSize.isChecked = settings.excludeSize != 0L
		enableExcludeRegularExpression.isChecked = settings.excludeRegularExpression != ""

		if (settings.excludeList.isEmpty())
		{
			excludeList.setSummary(R.string.summary_exclude_list_null)
		}
		else
		{
			excludeList.summary = getString(R.string.summary_exclude_list, settings.excludeList.size)
		}
		excludeList.isEnabled = settings.excludeList.isNotEmpty()
		if (settings.excludeNameList.isNotEmpty())
			excludeNameList.summary = getNameList(settings.excludeNameList)
		excludeNameList.isEnabled = settings.excludeNameList.isNotEmpty()
		if (settings.excludeSize != 0L)
			excludeSize.summary = (settings.excludeSize.toFloat() / 1048576f).toString()
		excludeSize.isEnabled = settings.excludeSize != 0L
		if (settings.excludeRegularExpression != "")
			excludeRegularExpression.summary = settings.excludeRegularExpression
		excludeRegularExpression.isEnabled = settings.excludeRegularExpression != ""

		if (settings.isAutoClean)
		{
			auto_clean.setSummary(R.string.summary_auto_clean_on)
		}
		else
		{
			auto_clean.setSummary(R.string.summary_auto_clean_off)
		}

		if (!settings.isDeveloperModeEnable)
		{
			preferenceScreen.removePreference(developerMode)
		}
		else
		{
			clickTime = 7
		}
	}

	private fun monitor()
	{
		auto_clean.setOnPreferenceChangeListener { _, _ ->
			val isAutoClean = !auto_clean.isChecked
			if (isAutoClean)
			{
				AlertDialog.Builder(this@SettingsActivity)
						.setTitle(" ")
						.setMessage(R.string.autoCleanWarn)
						.setPositiveButton(R.string.action_open) { _, _ -> settings.isAutoClean = true }
						.setNegativeButton(R.string.action_cancel) { _, _ ->
							auto_clean.isChecked = false
							settings.isAutoClean = false
						}
						.setOnDismissListener {
							auto_clean.isChecked = settings.isAutoClean
							if (settings.isAutoClean)
							{
								auto_clean.setSummary(R.string.summary_auto_clean_on)
							}
							else
							{
								auto_clean.setSummary(R.string.summary_auto_clean_off)
							}
						}
						.show()
			}
			else
			{
				settings.isAutoClean = false
				auto_clean.setSummary(R.string.summary_auto_clean_off)
			}
			true
		}
		enableExcludeList.setOnPreferenceChangeListener { _, _ ->
			val enableExcludeList = !enableExcludeList.isChecked
			if (enableExcludeList)
			{
				excludeList.summary = null
			}
			else
			{
				excludeList.setSummary(R.string.summary_exclude_list_null)
				settings.excludeList = emptySet()
			}
			excludeList.isEnabled = enableExcludeList
			true
		}
		excludeList.setOnPreferenceClickListener {
			progressDialog.show()
			Thread(Runnable {
				val allApp = AppManager.getAllApps(this)
				val message = Message()
				message.obj = allApp
				settingHandler.sendMessage(message)
			}).start()
			false
		}
		enableExcludeNameList.setOnPreferenceChangeListener { _, _ ->
			val enableExcludeName = !enableExcludeNameList.isChecked
			if (!enableExcludeName)
			{
				settings.excludeNameList = emptySet()
			}
			excludeNameList.isEnabled = enableExcludeName
			true
		}
		excludeNameList.setOnPreferenceClickListener {
			val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit, TextInputLayout(this), false)
			val text: TextInputLayout = view.findViewById(R.id.layout)
			text.editText!!.setText(getNameList(settings.excludeNameList))
			AlertDialog.Builder(this)
					.setTitle(R.string.hint_exclude_name_title)
					.setView(view)
					.setPositiveButton(R.string.action_done, { _, _ ->
						val temp = text.editText!!.text.toString()
						val set = HashSet<String>()
						temp.split(",")
								.filter { it != "" }
								.forEach {
									set.add(it)
								}
						settings.excludeNameList = set
					})
					.setNegativeButton(R.string.action_cancel, null)
					.show()
			false
		}
		enableExcludeSize.setOnPreferenceChangeListener { _, _ ->
			val enableExcludeSize = !enableExcludeSize.isChecked
			if (!enableExcludeSize)
			{
				settings.excludeSize = 0L
			}
			excludeSize.isEnabled = enableExcludeSize
			true
		}
		excludeSize.setOnPreferenceClickListener {
			val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit, TextInputLayout(this), false)
			val text: TextInputLayout = view.findViewById(R.id.layout)
			text.editText!!.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
			text.editText!!.setText((settings.excludeSize.toFloat() / 1048576f).toString())
			AlertDialog.Builder(this)
					.setTitle(R.string.hint_exclude_size_title)
					.setView(view)
					.setPositiveButton(R.string.action_done, { _, _ ->
						val temp = text.editText!!.text.toString().toFloat()
						if (temp > 0f)
						{
							settings.excludeSize = (temp * 1048576).toLong()
							excludeSize.summary = temp.toString()
						}
						else
							Snackbar.make(coordinatorLayout, R.string.hint_exclude_size_error, Snackbar.LENGTH_SHORT)
									.show()
					})
					.setNegativeButton(R.string.action_cancel, null)
					.show()
			false
		}
		enableExcludeRegularExpression.setOnPreferenceChangeListener { _, _ ->
			val enableExcludeRegularExpression = !enableExcludeRegularExpression.isChecked
			if (!enableExcludeRegularExpression)
			{
				settings.excludeRegularExpression = ""
			}
			excludeRegularExpression.isEnabled = enableExcludeRegularExpression
			true
		}
		excludeRegularExpression.setOnPreferenceClickListener {
			val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit, TextInputLayout(this), false)
			val text: TextInputLayout = view.findViewById(R.id.layout)
			text.editText!!.setText(settings.excludeRegularExpression)
			AlertDialog.Builder(this)
					.setTitle(R.string.hint_exclude_regular_expression_title)
					.setView(view)
					.setPositiveButton(R.string.action_done, { _, _ ->
						val pattern = text.editText!!.text.toString()
						try
						{
							Pattern.compile(pattern)
						}
						catch (e: PatternSyntaxException)
						{
							Snackbar.make(coordinatorLayout, R.string.hint_exclude_regular_expression_error, Snackbar.LENGTH_SHORT)
									.show()
							return@setPositiveButton
						}
						settings.excludeRegularExpression = pattern
						excludeRegularExpression.summary = pattern
					})
					.setNegativeButton(R.string.action_cancel, null)
					.show()
			false
		}
		developerModeEnable.setOnPreferenceChangeListener { _, _ ->
			val isDeveloperModeEnable = !developerModeEnable.isChecked
			settings.isDeveloperModeEnable = isDeveloperModeEnable
			true
		}
		autoUploadLog.setOnPreferenceChangeListener { _, _ ->
			val isAutoUploadLog = !autoUploadLog.isChecked
			settings.isAutoUploadLog = isAutoUploadLog
			true
		}
		about.setOnPreferenceClickListener {
			AlertDialog.Builder(this@SettingsActivity)
					.setTitle(" ")
					.setView(LayoutInflater.from(this@SettingsActivity).inflate(R.layout.dialog_about, LinearLayout(this@SettingsActivity), false))
					.setPositiveButton(R.string.action_close, null)
					.show()
			false
		}
		howToUse.setOnPreferenceClickListener {
			val view = LayoutInflater.from(this@SettingsActivity).inflate(R.layout.dialog_help, LinearLayout(this@SettingsActivity), false)
			val textView = view.findViewById<TextView>(R.id.autoCleanWarn)
			if (settings.isAutoClean)
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
		openSourceAddress.setOnPreferenceClickListener {
			val intent = Intent(Intent.ACTION_VIEW)
			val content_url = Uri.parse(getString(R.string.address_open_source))
			intent.data = content_url
			startActivity(intent)
			false
		}
		license.setOnPreferenceClickListener {
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
						settings.isFirst = false
					})
					.show()
			false
		}
		checkUpdate.setOnPreferenceClickListener {
			val intent = Intent(Intent.ACTION_VIEW)
			val content_url = Uri.parse(getString(R.string.address_check_update))
			intent.data = content_url
			startActivity(intent)
			false
		}
		versionCode.setOnPreferenceClickListener {
			when
			{
				clickTime < 3 ->
					clickTime++
				clickTime in 3..6 ->
				{
					val hintToast = Toast.makeText(this, String.format(getString(R.string.hint_developer_mode), 7 - clickTime), Toast.LENGTH_SHORT)
					hintToast.show()
					Timer().schedule(timerTask {
						hintToast.cancel()
					}, 100)
					clickTime++
				}
				clickTime >= 7 ->
				{
					if (!settings.isDeveloperModeEnable)
					{
						preferenceScreen.addPreference(developerMode)
						settings.isDeveloperModeEnable = true
						developerModeEnable.isChecked = true
					}
					val hintToast = Toast.makeText(this, R.string.hint_developer_mode_enable, Toast.LENGTH_SHORT)
					hintToast.show()
					Timer().schedule(timerTask {
						hintToast.cancel()
					}, 1000)
				}
			}
			false
		}
		support.setOnPreferenceClickListener {
			AlertDialog.Builder(this)
					.setTitle(R.string.pay_method_title)
					.setItems(R.array.pay_method, { _, choose ->
						val message = Message()
						when (choose)
						{
							0 ->
							{
								message.what = PayHandler.PAY_PLAY
								isGooglePlayPay = true
							}
							1 ->
							{
								message.what = PayHandler.PAY_ALIPAY
								isGooglePlayPay = false
							}
							2 ->
							{
								message.what = PayHandler.PAY_WEIXIN
								isGooglePlayPay = false
							}
						}
						payHandler.sendMessage(message)
					})
					.show()
			false
		}
		joinTest.setOnPreferenceClickListener {
			val intent = Intent(Intent.ACTION_VIEW)
			AlertDialog.Builder(this)
					.setItems(R.array.become_test, { _, choose ->
						when (choose)
						{
							0 ->
							{
								intent.data = Uri.parse(getString(R.string.address_join_test_group))
							}
							1 ->
							{
								intent.data = Uri.parse(getString(R.string.address_become_test))
							}
						}
						startActivity(intent)
					})
					.show()
			false
		}
	}

	private fun getNameList(list: Set<String>): String
	{
		var tempString = ""
		var index = 0
		list.forEach {
			tempString += it
			index++
			if (index != list.size)
				tempString += ","
		}
		return tempString
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

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		if (!isGooglePlayPay && payHandler.onPayResult(requestCode, resultCode, data))
			super.onActivityResult(requestCode, resultCode, data)
		else
			Logs.i(TAG, "onActivityResult handled by IABUtil.")
	}

	override fun onDestroy()
	{
		try
		{
			payHandler.playDestroy()
		}
		catch (e: Exception)
		{
			Logs.e(TAG, "onDestroy: 销毁失败")
		}
		super.onDestroy()
	}
}
