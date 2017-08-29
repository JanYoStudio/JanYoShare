package com.janyo.janyoshare.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.CustomFormat
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.handler.SettingHandler
import com.janyo.janyoshare.util.AppManager
import com.janyo.janyoshare.util.Settings
import com.mystery0.tools.SnackBar.ASnackBar
import dmax.dialog.SpotsDialog
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.concurrent.timerTask

class SettingsPreferenceFragment : PreferenceFragment()
{
	private lateinit var settings: Settings
	private lateinit var auto_clean: SwitchPreference
	private lateinit var enableExcludeList: SwitchPreference
	private lateinit var excludeList: Preference
	private lateinit var customNameFormat: Preference
	private lateinit var longClickDo: Preference
	private lateinit var developerMode: PreferenceCategory
	private lateinit var developerModeEnable: SwitchPreference
	private lateinit var enableExcludeNameList: SwitchPreference
	private lateinit var excludeNameList: Preference
	private lateinit var enableExcludeSize: SwitchPreference
	private lateinit var excludeSize: Preference
	private lateinit var enableExcludeRegularExpression: SwitchPreference
	private lateinit var excludeRegularExpression: Preference
	private lateinit var autoUploadLog: SwitchPreference
	private lateinit var disableAccessibility: SwitchPreference
	private lateinit var disableIcon: SwitchPreference
	private lateinit var about: Preference
	private lateinit var howToUse: Preference
	private lateinit var openSourceAddress: Preference
	private lateinit var checkUpdate: Preference
	private lateinit var versionCode: Preference
	private lateinit var joinTest: Preference
	private lateinit var homePage: Preference
	private lateinit var coordinatorLayout: CoordinatorLayout
	private lateinit var settingHandler: SettingHandler
	private lateinit var progressDialog: SpotsDialog
	private var clickTime = 0

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		settings = Settings.getInstance(activity)
		addPreferencesFromResource(R.xml.preferences)
		initialization()
		monitor()
	}

	private fun initialization()
	{
		auto_clean = findPreference(getString(R.string.key_auto_clean)) as SwitchPreference
		enableExcludeList = findPreference(getString(R.string.key_enable_exclude_list)) as SwitchPreference
		excludeList = findPreference(getString(R.string.key_exclude_list))
		customNameFormat = findPreference(getString(R.string.key_custom_name_format))
		longClickDo = findPreference(getString(R.string.key_long_click_do))
		developerMode = findPreference(getString(R.string.key_developer_mode)) as PreferenceCategory
		developerModeEnable = findPreference(getString(R.string.key_developer_mode_enable)) as SwitchPreference
		enableExcludeNameList = findPreference(getString(R.string.key_enable_exclude_name)) as SwitchPreference
		excludeNameList = findPreference(getString(R.string.key_exclude_name_list))
		enableExcludeSize = findPreference(getString(R.string.key_enable_exclude_size)) as SwitchPreference
		excludeSize = findPreference(getString(R.string.key_exclude_size))
		enableExcludeRegularExpression = findPreference(getString(R.string.key_enable_exclude_regular_expression)) as SwitchPreference
		excludeRegularExpression = findPreference(getString(R.string.key_exclude_regular_expression))
		autoUploadLog = findPreference(getString(R.string.key_auto_upload_log)) as SwitchPreference
		disableAccessibility = findPreference(getString(R.string.key_disable_accessibility)) as SwitchPreference
		disableIcon = findPreference(getString(R.string.key_disable_icon)) as SwitchPreference
		about = findPreference(getString(R.string.key_about))
		howToUse = findPreference(getString(R.string.key_how_to_use))
		openSourceAddress = findPreference(getString(R.string.key_open_source_address))
		checkUpdate = findPreference(getString(R.string.key_check_update))
		versionCode = findPreference(getString(R.string.key_version_code))
		joinTest = findPreference(getString(R.string.key_join_test))
		homePage = findPreference(getString(R.string.key_home_page))

		progressDialog = SpotsDialog(activity, R.style.SpotsDialog)
		progressDialog.setMessage(getString(R.string.hint_exclude_list_loading))
		progressDialog.setCancelable(false)
		settingHandler = SettingHandler(activity, progressDialog, excludeList)

		auto_clean.isChecked = settings.isAutoClean
		developerModeEnable.isChecked = settings.isDeveloperModeEnable
		autoUploadLog.isChecked = settings.isAutoUploadLog
		disableAccessibility.isChecked = settings.isDisableAccessibility
		disableIcon.isChecked = settings.isDisableIcon
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
		customNameFormat.summary = settings.customFileName.format
		longClickDo.summary = resources.getStringArray(R.array.long_click_do)[settings.longClickDo]
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
				AlertDialog.Builder(activity)
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
				val allApp = AppManager.getAllApps(activity)
				val message = Message()
				message.obj = allApp
				settingHandler.sendMessage(message)
			}).start()
			false
		}
		customNameFormat.setOnPreferenceClickListener {
			val view = LayoutInflater.from(activity).inflate(R.layout.dialog_custom_name_format, LinearLayout(activity), false)
			val textInputLayout: TextInputLayout = view.findViewById(R.id.textInputLayout)
			val showText: TextView = view.findViewById(R.id.show)
			textInputLayout.editText!!.setText(settings.customFileName.format)
			showText.text = getString(R.string.hint_custom_name_format_show, "")
			val testApp = InstallApp()
			testApp.name = getString(R.string.app_name)
			testApp.versionName = getString(R.string.app_version)
			testApp.versionCode = getString(R.string.app_version_code).toInt()
			testApp.packageName = getString(R.string.app_package_name)
			textInputLayout.editText!!.addTextChangedListener(object : TextWatcher
			{
				override fun afterTextChanged(p0: Editable)
				{
					val format = CustomFormat(p0.toString())
					showText.text = getString(R.string.hint_custom_name_format_show, format.toFormat(testApp))
				}

				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
				{
				}

				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
				{
				}
			})
			AlertDialog.Builder(activity)
					.setTitle(R.string.hint_custom_name_format_title)
					.setView(view)
					.setPositiveButton(R.string.action_done, { _, _ ->
						val temp = textInputLayout.editText!!.text.toString()
						settings.customFileName = CustomFormat(temp)
						customNameFormat.summary = temp
					})
					.setNegativeButton(R.string.action_cancel, null)
					.show()
			false
		}
		longClickDo.setOnPreferenceClickListener {
			AlertDialog.Builder(activity)
					.setTitle(R.string.hint_long_click_do_title)
					.setItems(R.array.long_click_do, { _, position ->
						settings.longClickDo = position
						longClickDo.summary = resources.getStringArray(R.array.long_click_do)[settings.longClickDo]
					})
					.show()
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
			val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit, TextInputLayout(activity), false)
			val text: TextInputLayout = view.findViewById(R.id.layout)
			text.editText!!.setText(getNameList(settings.excludeNameList))
			AlertDialog.Builder(activity)
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
			val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit, TextInputLayout(activity), false)
			val text: TextInputLayout = view.findViewById(R.id.layout)
			text.editText!!.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
			text.editText!!.setText((settings.excludeSize.toFloat() / 1048576f).toString())
			AlertDialog.Builder(activity)
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
			val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit, TextInputLayout(activity), false)
			val text: TextInputLayout = view.findViewById(R.id.layout)
			text.editText!!.setText(settings.excludeRegularExpression)
			AlertDialog.Builder(activity)
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
		disableAccessibility.setOnPreferenceChangeListener { _, _ ->
			val isDisableAccessibility = !disableAccessibility.isChecked
			if (isDisableAccessibility)
			{
				AlertDialog.Builder(activity)
						.setTitle(" ")
						.setMessage(R.string.disableAccessibilityWarn)
						.setPositiveButton(R.string.action_open) { _, _ -> settings.isDisableAccessibility = true }
						.setNegativeButton(R.string.action_cancel) { _, _ ->
							disableAccessibility.isChecked = false
							settings.isDisableAccessibility = false
						}
						.setOnDismissListener {
							disableAccessibility.isChecked = settings.isDisableAccessibility
							if (settings.isDisableAccessibility)
							{
								ASnackBar.disableAccessibility(activity)
							}
						}
						.show()
			}
			else
			{
				settings.isDisableAccessibility = false
			}
			true
		}
		disableIcon.setOnPreferenceChangeListener { _, _ ->
			val isDisableIcon = !disableIcon.isChecked
			settings.isDisableIcon = isDisableIcon
			true
		}
		about.setOnPreferenceClickListener {
			AlertDialog.Builder(activity)
					.setTitle(" ")
					.setView(LayoutInflater.from(activity).inflate(R.layout.dialog_about, LinearLayout(activity), false))
					.setPositiveButton(R.string.action_close, null)
					.show()
			false
		}
		howToUse.setOnPreferenceClickListener {
			val view = LayoutInflater.from(activity).inflate(R.layout.dialog_help, LinearLayout(activity), false)
			val textView = view.findViewById<TextView>(R.id.autoCleanWarn)
			if (settings.isAutoClean)
			{
				textView.visibility = View.VISIBLE
			}
			AlertDialog.Builder(activity)
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
					val hintToast = Toast.makeText(activity, String.format(getString(R.string.hint_developer_mode), 7 - clickTime), Toast.LENGTH_SHORT)
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
					val hintToast = Toast.makeText(activity, R.string.hint_developer_mode_enable, Toast.LENGTH_SHORT)
					hintToast.show()
					Timer().schedule(timerTask {
						hintToast.cancel()
					}, 1000)
				}
			}
			false
		}
		joinTest.setOnPreferenceClickListener {
			val intent = Intent(Intent.ACTION_VIEW)
			AlertDialog.Builder(activity)
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
		homePage.setOnPreferenceClickListener {
			val intent = Intent(Intent.ACTION_VIEW)
			val content_url = Uri.parse(getString(R.string.address_home_page))
			intent.data = content_url
			startActivity(intent)
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
}