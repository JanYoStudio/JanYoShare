package com.janyo.janyoshare.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.Error
import com.janyo.janyoshare.classes.Response
import com.janyo.janyoshare.handler.UploadLogHandler
import com.janyo.janyoshare.util.ExceptionUtil
import com.janyo.janyoshare.util.Settings
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_error.*
import vip.mystery0.tools.hTTPok.HTTPokResponse
import vip.mystery0.tools.hTTPok.HTTPokResponseListener
import vip.mystery0.tools.logs.Logs
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashMap

class ErrorActivity : AppCompatActivity()
{
	private val TAG = "ErrorActivity"
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
			val map = HashMap<String, Any>()
			map.put("logFile", intent.getBundleExtra("error").getSerializable("file"))
			map.put("date", error.time)
			map.put("appName", getString(R.string.app_name))
			map.put("appVersionName", error.appVersionName)
			map.put("appVersionCode", error.appVersionCode.toString())
			map.put("androidVersion", error.AndroidVersion)
			map.put("sdk", error.sdk.toString())
			map.put("vendor", error.vendor)
			map.put("model", error.model)
			ExceptionUtil.sendException(map, "http://janyo.pw/uploadLog.php",
					object : HTTPokResponseListener
					{
						override fun onError(message: String?)
						{
							Logs.e(TAG, "onError: " + message)
							ExceptionUtil.tryOther(map, uploadLogHandler)
						}

						override fun onResponse(response: HTTPokResponse)
						{
							val response1 = response.getJSON(Response::class.java)
							if (response1.code == 0)
							{
								uploadLogHandler.response = response1
								uploadLogHandler.sendEmptyMessage(0)
							}
							else
							{
								ExceptionUtil.tryOther(map, uploadLogHandler)
							}
						}
					})
		}
	}
}