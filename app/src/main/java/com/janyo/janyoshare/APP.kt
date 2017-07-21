package com.janyo.janyoshare

import android.app.Application
import com.mystery0.tools.CrashHandler.CrashHandler
import com.mystery0.tools.Logs.Logs

class APP : Application()
{
	override fun onCreate()
	{
		super.onCreate()
		Logs.setLevel(Logs.LogLevel.Debug)
		CrashHandler.getInstance(applicationContext)
				.init()
	}
}