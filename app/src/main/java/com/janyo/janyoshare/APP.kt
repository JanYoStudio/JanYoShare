package com.janyo.janyoshare

import android.app.Application
import android.content.Context
import com.mystery0.tools.CrashHandler.CrashHandler
import com.mystery0.tools.Logs.Logs
import java.io.File



class APP : Application()
{
	var context: Context? = null

	override fun onCreate()
	{
		super.onCreate()
		this.context = applicationContext
		Logs.setLevel(Logs.LogLevel.Debug)
		CrashHandler.getInstance(applicationContext)
				.setDirectory(getString(R.string.app_name) + File.separator + "log")
				.setPrefixName("crash")
				.setExtensionName("log")
				.isAutoClean(true)
				.init()
	}
}