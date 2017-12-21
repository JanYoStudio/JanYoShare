package com.janyo.janyoshare

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import com.janyo.janyoshare.activity.ErrorActivity
import com.janyo.janyoshare.activity.MainActivity
import com.janyo.janyoshare.classes.Error
import com.janyo.janyoshare.classes.Response
import com.janyo.janyoshare.util.ExceptionUtil
import com.janyo.janyoshare.util.Settings
import vip.mystery0.tools.crashHandler.CatchExceptionListener
import vip.mystery0.tools.crashHandler.CrashHandler
import vip.mystery0.tools.hTTPok.HTTPokResponse
import vip.mystery0.tools.hTTPok.HTTPokResponseListener
import vip.mystery0.tools.logs.Logs
import vip.mystery0.tools.snackBar.ASnackBar
import java.io.File
import java.util.HashMap

class APP : Application()
{
	private val TAG = "APP"
	override fun onCreate()
	{
		super.onCreate()
		Logs.setLevel(Logs.LogLevel.Release)
		CrashHandler.getInstance(this)
				.setDirectory(getString(R.string.app_name) + File.separator + "log")
				.setPrefixName("crash")
				.setExtensionName("log")
				.isAutoClean(2)
				.sendException(object : CatchExceptionListener {
					override fun onException(date: String, file: File, appVersionName: String,
											 appVersionCode: Int, AndroidVersion: String,
											 sdk: Int, vendor: String, model: String, ex: Throwable) {
						if (Settings.isAutoUploadLog) {
							val map = HashMap<String, String>()
							val fileMap = HashMap<String, File>()
							fileMap.put("logFile", file)
							map.put("date", date)
							map.put("appName", getString(R.string.app_name))
							map.put("appVersionName", appVersionName)
							map.put("appVersionCode", appVersionCode.toString())
							map.put("androidVersion", AndroidVersion)
							map.put("sdk", sdk.toString())
							map.put("vendor", vendor)
							map.put("model", model)
							ExceptionUtil.sendException(map, "http://janyo.pw/uploadLog.php",
									object : HTTPokResponseListener {
										override fun onError(message: String?) {
											Logs.e(TAG, "onError: " + message)
											ExceptionUtil.tryOther(applicationContext, map)
										}

										override fun onResponse(response: HTTPokResponse?) {
											val response1 = response?.getJSON(Response::class.java)
											if (response1?.code == 0) {
												Toast.makeText(applicationContext, R.string.hint_upload_log_done, Toast.LENGTH_SHORT)
														.show()
											} else {
												ExceptionUtil.tryOther(applicationContext, map)
											}
										}
									})
						} else {
							val error = Error(date, appVersionName, appVersionCode, AndroidVersion, sdk, vendor, model, ex)
							val bundle = Bundle()
							bundle.putSerializable("file", file)
							bundle.putSerializable("error", error)
							val intent = Intent(applicationContext, ErrorActivity::class.java)
							intent.putExtra("error", bundle)
							startActivity(intent)
						}
					}
				})
				.init()
		val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
		val list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
		if (Settings.isDisableAccessibility && list.isNotEmpty())
		{
			Logs.i(TAG, "onCreate: 开启无障碍服务，尝试禁用")
			ASnackBar.disableAccessibility(this)
		}
		else
		{
			Logs.i(TAG, "onCreate: 未开启无障碍服务或者关闭强制")
		}
	}

	companion object
	{
		private var app: APP? = null

		fun getInstance(): APP
		{
			if (app == null)
				app = APP()
			return app!!
		}
	}
}