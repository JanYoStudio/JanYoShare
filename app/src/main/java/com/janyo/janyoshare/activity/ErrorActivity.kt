package com.janyo.janyoshare.activity

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import com.android.volley.toolbox.Volley
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.Error
import com.janyo.janyoshare.util.Settings
import com.mystery0.tools.Logs.Logs
import com.mystery0.tools.MysteryNetFrameWork.HttpUtil
import com.mystery0.tools.MysteryNetFrameWork.ResponseListener
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_error.*
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashMap

class ErrorActivity : Activity()
{
	private val TAG = "ErrorActivity"
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_error)

		if (intent.getBundleExtra("error") == null)
			finish()
		val settings = Settings.getInstance(this)
		val error = intent.getBundleExtra("error").getSerializable("error") as Error
		Logs.i(TAG, "onCreate: " + error.appVersionName)
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
		button.setOnClickListener {
			val progressDialog = SpotsDialog(this)
			progressDialog.setMessage(getString(R.string.hint_upload_log))
			progressDialog.show()
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
			HttpUtil(this)
					.setRequestQueue(Volley.newRequestQueue(applicationContext))
					.setUrl("http://123.206.186.70/php/uploadLog/upload_file.php")
					.setRequestMethod(HttpUtil.RequestMethod.POST)
					.setFileRequest(HttpUtil.FileRequest.UPLOAD)
					.isFileRequest(true)
					.setMap(map)
					.setFileMap(fileMap)
					.setResponseListener(object : ResponseListener
					{
						override fun onResponse(code: Int, message: String?)
						{
							if (code == 0)
							{
								progressDialog.dismiss()
								Toast.makeText(applicationContext, R.string.hint_upload_log_done, Toast.LENGTH_SHORT)
										.show()
								finish()
							}
							else
							{
								Logs.e(TAG, "onResponse: " + message)
							}
						}
					})
					.open()
		}
	}
}