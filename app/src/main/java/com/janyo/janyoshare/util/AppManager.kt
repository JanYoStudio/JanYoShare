package com.janyo.janyoshare.util

import android.content.Context
import android.content.pm.ApplicationInfo

import com.janyo.janyoshare.classes.InstallApp

import java.io.File
import java.util.ArrayList
import java.util.Collections
import java.util.regex.Pattern

object AppManager
{
	var SYSTEM = 1
	var USER = 2

	fun getInstallAppList(context: Context, appType: Int, type: Int,
						  isExclude: Boolean): List<InstallApp>
	{
		val packageManager = context.packageManager
		val packageInfoList = packageManager.getInstalledPackages(0)
		val installAppList = ArrayList<InstallApp>()
		when (appType)
		{
			SYSTEM -> for (i in packageInfoList.indices)
			{
				val installApp = InstallApp()
				val packageInfo = packageInfoList[i]
				if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM > 0)
				{
					installApp.name = packageInfo.applicationInfo.loadLabel(packageManager).toString()
					installApp.versionName = packageInfo.versionName
					installApp.versionCode = packageInfo.versionCode
					installApp.sourceDir = packageInfo.applicationInfo.sourceDir
					installApp.packageName = packageInfo.applicationInfo.packageName
					val path = context.cacheDir.absolutePath + File.separator + "icon" + File.separator + installApp.packageName
					if (JYFileUtil.saveDrawableToSd(packageInfo.applicationInfo.loadIcon(packageManager), path))
					{
						installApp.iconPath = path
					}
					else
					{
						installApp.icon = packageInfo.applicationInfo.loadIcon(packageManager)
					}
					installApp.size = File(packageInfo.applicationInfo.publicSourceDir).length()
					installApp.installTime = packageInfo.firstInstallTime
					installApp.updateTime = packageInfo.lastUpdateTime
					if (!isExclude || checkExclude(context, installApp))
					{
						installAppList.add(installApp)
					}
				}
			}
			USER -> for (i in packageInfoList.indices)
			{
				val installApp = InstallApp()
				val packageInfo = packageInfoList[i]
				if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM <= 0)
				{
					installApp.name = packageInfo.applicationInfo.loadLabel(packageManager).toString()
					installApp.versionName = packageInfo.versionName
					installApp.versionCode = packageInfo.versionCode
					installApp.sourceDir = packageInfo.applicationInfo.sourceDir
					installApp.packageName = packageInfo.applicationInfo.packageName
					val path = context.cacheDir.absolutePath + File.separator + "icon" + File.separator + installApp.packageName
					if (JYFileUtil.saveDrawableToSd(packageInfo.applicationInfo.loadIcon(packageManager), path))
					{
						installApp.iconPath = path
					}
					else
					{
						installApp.icon = packageInfo.applicationInfo.loadIcon(packageManager)
					}
					installApp.size = File(packageInfo.applicationInfo.publicSourceDir).length()
					installApp.installTime = packageInfo.firstInstallTime
					installApp.updateTime = packageInfo.lastUpdateTime
					if (!isExclude || checkExclude(context, installApp))
					{
						installAppList.add(installApp)
					}
				}
			}
		}
		if (type == 0)
			return installAppList
		return sortList(installAppList, type)
	}

	fun searchApps(sourceList: List<InstallApp>, message: String): List<InstallApp>
	{
		return sourceList.filter { it.name!!.toLowerCase().contains(message.toLowerCase()) }
	}

	fun getAllApps(context: Context): List<InstallApp>
	{
		val list = ArrayList<InstallApp>()
		list.addAll(getInstallAppList(context, SYSTEM, 0, false))
		list.addAll(getInstallAppList(context, USER, 0, false))
		return sortList(list, 1)
	}

	private fun checkExclude(context: Context, installApp: InstallApp): Boolean
	{
		val settings = Settings.getInstance(context)
		val excludeList = settings.excludeList
		val excludeNameList = settings.excludeNameList
		val excludeSize = settings.excludeSize
		val excludeRegularExpression = settings.excludeRegularExpression
		if (excludeList.contains(installApp.packageName!!) ||
				installApp.size < excludeSize)
			return false
		excludeNameList.forEach {
			if (installApp.name!!.contains(it))
				return false
		}
		if (Pattern.matches(excludeRegularExpression, installApp.name) || Pattern.matches(excludeRegularExpression, installApp.packageName))
			return false
		return true
	}

	private fun sortList(list: List<InstallApp>, type: Int): List<InstallApp>
	{
		val objects = list.toTypedArray()
		val installApps = arrayOfNulls<InstallApp>(objects.size)
		for (i in objects.indices)
		{
			installApps[i] = objects[i]
		}
		quickSort(installApps, 0, list.size - 1, type)
		val installAppList = ArrayList<InstallApp>()
		Collections.addAll<InstallApp>(installAppList, *installApps)
		return installAppList
	}

	private fun quickSort(A: Array<InstallApp?>, begin: Int, end: Int, type: Int)
	{
		if (begin < end)
		{
			val q: Int = partition(A, begin, end, type)
			quickSort(A, begin, q - 1, type)
			quickSort(A, q + 1, end, type)
		}
	}

	private fun partition(A: Array<InstallApp?>, l: Int, h: Int, type: Int): Int
	{
		var low = l
		var high = h
		val pivot = A[low]
		when (type)
		{
			1 -> while (low < high)
			{
				while (low < high && A[high]!!.name!!.compareTo(pivot!!.name!!, ignoreCase = true) >= 0)
					--high
				A[low] = A[high]
				while (low < high && A[low]!!.name!!.compareTo(pivot!!.name!!, ignoreCase = true) <= 0)
					++low
				A[high] = A[low]
			}
			2 -> while (low < high)
			{
				while (low < high && A[high]!!.size >= pivot!!.size)
					--high
				A[low] = A[high]
				while (low < high && A[low]!!.size <= pivot!!.size)
					++low
				A[high] = A[low]
			}
			3 -> while (low < high)
			{
				while (low < high && A[high]!!.packageName!!.compareTo(pivot!!.packageName!!, ignoreCase = true) >= 0)
					--high
				A[low] = A[high]
				while (low < high && A[low]!!.packageName!!.compareTo(pivot!!.packageName!!, ignoreCase = true) <= 0)
					++low
				A[high] = A[low]
			}
			4 -> while (low < high)
			{
				while (low < high && A[high]!!.installTime <= pivot!!.installTime)
					--high
				A[low] = A[high]
				while (low < high && A[low]!!.installTime >= pivot!!.installTime)
					++low
				A[high] = A[low]
			}
			5 -> while (low < high)
			{
				while (low < high && A[high]!!.updateTime <= pivot!!.updateTime)
					--high
				A[low] = A[high]
				while (low < high && A[low]!!.updateTime >= pivot!!.updateTime)
					++low
				A[high] = A[low]
			}
		}
		A[low] = pivot
		return low
	}
}