package com.janyo.janyoshare.classes

import android.graphics.drawable.Drawable
import java.io.Serializable

class InstallApp : Serializable
{
	var name: String? = null
	var versionName: String? = null
	var versionCode: Int = 0
	var sourceDir: String? = null
	var packageName: String? = null
	var iconPath: String? = null
	var icon: Drawable? = null
	var size: Long = 0
	var installTime: Long = 0
	var updateTime: Long = 0
}

/**
 * N——appName
 * V——versionName
 * W——versionCode
 * P——packageName
 */
