package com.janyo.janyoshare

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.view.accessibility.AccessibilityManager
import com.mystery0.tools.CrashHandler.CrashHandler
import com.mystery0.tools.Logs.Logs
import com.mystery0.tools.SnackBar.ASnackBar
import java.io.File

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
				.init()
		try
		{
			val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
			val list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
			if (list.isNotEmpty())
			{
				Logs.i(TAG, "onCreate: 开启无障碍服务，尝试禁用")
				ASnackBar.disableAccessibility(this)
			}
			else
			{
				Logs.i(TAG, "onCreate: 未开启无障碍服务")
			}
		}
		catch (e: Exception)
		{
			Logs.e(TAG, "onCreate: $e")
		}
	}
}