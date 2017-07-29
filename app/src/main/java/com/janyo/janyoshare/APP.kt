package com.janyo.janyoshare

import android.app.Application
import com.mystery0.tools.CrashHandler.CrashHandler
import com.mystery0.tools.Logs.Logs
import java.io.File

class APP : Application()
{
	override fun onCreate()
	{
		super.onCreate()
		Logs.setLevel(Logs.LogLevel.Release)
		CrashHandler.getInstance(applicationContext)
				.setDirectory(getString(R.string.app_name) + File.separator + "log")
				.setPrefixName("crash")
				.setExtensionName("log")
				.isAutoClean(2)
				.init()
	}
}