package com.janyo.janyoshare.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.google.gson.Gson
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.Error
import com.janyo.janyoshare.classes.Response
import com.janyo.janyoshare.handler.UploadLogHandler
import com.janyo.janyoshare.util.ExceptionUtil
import com.janyo.janyoshare.util.Settings
import vip.mystery0.tools.MysteryNetFrameWork.ResponseListener
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_error.*
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashMap

class ErrorActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		val settings = Settings.getInstance(this)
		if (settings.dayNight)
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		else
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_error)

		val spotsDialog = SpotsDialog(this, getString(R.string.hint_upload_log), R.style.SpotsDialog)
		val uploadLogHandler = UploadLogHandler()
		uploadLogHandler.spotsDialog = spotsDialog
		uploadLogHandler.activity = this
		uploadLogHandler.coordinatorLayout = coordinatorLayout

		if (intent.getBundleExtra("error") == null)
			finish()
		val error = intent.getBundleExtra("error").getSerializable("error") as Error

		text_date.text = getString(R.string.exception_time, error.time)
		text_version.text = getString(R.string.exception_version, error.appVersionName, error.appVersionCode)
		text_SDK.text = getString(R.string.exception_sdk, error.AndroidVersion, error.sdk)
		text_vendor.text = getString(R.string.exception_vendor, error.vendor)
		text_model.text = getString(R.string.exception_model, error.model)
		val stringWriter = StringWriter()
		error.ex.printStackTrace(PrintWriter(stringWriter))
		text_exception.text = getString(R.string.exception_message, stringWriter.toString())
		Switch.isChecked = settings.isAutoUploadLog
		Switch.setOnCheckedChangeListener { _, checked ->
			settings.isAutoUploadLog = checked
		}
		button_exit.setOnClickListener {
			System.exit(0)
		}
		button_restart.setOnClickListener {
			finish()
		}
		button_upload.setOnClickListener {
			spotsDialog.show()
			val map = HashMap<String, String>()
			val fileMap = HashMap<String, File>()
			fileMap.put("logFile", intent.getBundleExtra("error").getSerializable("file") as File)
			map.put("date", error.time)
			map.put("appName", getString(R.string.app_name))
			map.put("appVersionName", error.appVersionName)
			map.put("appVersionCode", error.appVersionCode.toString())
			map.put("androidVersion", error.AndroidVersion)
			map.put("sdk", error.sdk.toString())
			map.put("vendor", error.vendor)
			map.put("model", error.model)
			ExceptionUtil.sendException(this, map, fileMap, "http://janyo.pw/uploadLog.php",
					object : ResponseListener
					{
						override fun onResponse(code: Int, message: String?)
						{
							val response1 = Gson().fromJson(message, Response::class.java)
							if (response1.code == 0)
							{
								uploadLogHandler.response = response1
								uploadLogHandler.sendEmptyMessage(0)
							}
							else
							{
								ExceptionUtil.sendException(this@ErrorActivity, map, fileMap, "http://123.206.186.70/php/uploadLog/upload_file.php",
										object : ResponseListener
										{
											override fun onResponse(code: Int, message: String?)
											{
												try
												{
													val response = Gson().fromJson(message, Response::class.java)
													uploadLogHandler.response = response
												}
												catch (e: Exception)
												{
												}
												uploadLogHandler.sendEmptyMessage(0)
											}
										})
							}
						}
					})
		}
	}
}