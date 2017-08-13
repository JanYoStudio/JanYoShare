package com.janyo.janyoshare.activity

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.util.Settings

class DisableAccessibilityActivity : Activity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val settings = Settings.getInstance(this)
		if (settings.isDisableAccessibility)
		{
			settings.isDisableAccessibility = false
			Toast.makeText(this, R.string.hint_disable_accessibility_dis, Toast.LENGTH_SHORT)
					.show()
		}
		else
		{
			Toast.makeText(this, R.string.hint_disable_accessibility, Toast.LENGTH_SHORT)
					.show()
		}
		finish()
	}
}