package com.janyo.janyoshare.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.Error
import com.mystery0.tools.Logs.Logs

class ErrorActivity : AppCompatActivity()
{
	private val TAG = "ErrorActivity"
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_error)

		if (intent.getBundleExtra("error") == null)
			finish()
		val error = intent.getBundleExtra("error").getSerializable("error") as Error
		Logs.i(TAG, "onCreate: " + error.appVersionName)
	}
}