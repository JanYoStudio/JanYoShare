package com.janyo.janyoshare.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.FileProvider
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.CustomFormat
import com.janyo.janyoshare.classes.InstallApp
import com.mystery0.tools.Logs.Logs
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

object JYFileUtil
{
	private val TAG = "JYFileUtil"

	fun cleanIconCache(context: Context)
	{
		val dirPath = context.cacheDir.absolutePath + File.separator + "icon" + File.separator
		val dir = File(dirPath)
		if (dir.exists())
			dir.delete()
	}

	fun cleanFileDir(dir: String): Boolean
	{
		val file = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + dir + File.separator)
		if (!file.exists())
		{
			file.mkdirs()
		}
		if (file.isDirectory)
		{
			file.listFiles().forEach { deleteFile(it) }
			return true
		}
		return false
	}

	fun deleteFile(file: File): Boolean
	{
		if (file.isFile)
			return file.delete()
		return true
	}

	fun getSaveFilePath(fileName: String, dir: String): String
	{
		return File(Environment.getExternalStoragePublicDirectory(dir), fileName).absolutePath
	}

	fun getFilePath(format: CustomFormat, installApp: InstallApp, dir: String,
					extensionName: String): File
	{
		return File(Environment.getExternalStoragePublicDirectory(dir), format.toFormat(installApp) + "." + extensionName)
	}

	fun getFilePath(installApp: InstallApp, dir: String, extensionName: String): File
	{
		return File(Environment.getExternalStoragePublicDirectory(dir), installApp.name + "_" + installApp.versionName + "." + extensionName)
	}

	fun fileToSD(inputPath: String, format: CustomFormat, installApp: InstallApp, dir: String,
				 extensionName: String): Int
	{
		return fileCopy(inputPath, File(Environment.getExternalStoragePublicDirectory(dir), format.toFormat(installApp) + "." + extensionName).absolutePath)
	}

	private fun fileToSD(inputPath: String, fileName: String, dir: String,
						 extensionName: String): Int
	{
		return fileCopy(inputPath, File(Environment.getExternalStoragePublicDirectory(dir), fileName + "." + extensionName).absolutePath)
	}

	fun fileToSD(inputPath: String, installApp: InstallApp, dir: String, extensionName: String): Int
	{
		return fileToSD(inputPath, installApp.name + "_" + installApp.versionName, dir, extensionName)
	}

	private fun fileCopy(inputPath: String, outPath: String): Int
	{
		if (File(outPath).exists())
			return 0
		var code = -1
		try
		{
			val fileInputStream = FileInputStream(inputPath)
			val fileOutputStream = FileOutputStream(outPath)
			val bytes = ByteArray(1024 * 1024 * 10)
			var ReadCount = 0
			while (ReadCount != -1)
			{
				fileOutputStream.write(bytes, 0, ReadCount)
				ReadCount = fileInputStream.read(bytes)
			}
			fileInputStream.close()
			code = 1
		}
		catch (e: IOException)
		{
			e.printStackTrace()
		}
		return code
	}

	fun fileRename(oldName: String, dir: String, newName: String, oldExtensionName: String,
				   newExtensionName: String): Boolean
	{
		val file = File(Environment.getExternalStoragePublicDirectory(dir), oldName + "." + oldExtensionName)
		return file.renameTo(File(Environment.getExternalStoragePublicDirectory(dir), newName + "." + newExtensionName))
	}

	fun isDirExist(dir: String): Boolean
	{
		val sdcard = Environment.getExternalStorageDirectory().absolutePath + File.separator
		val file = File(sdcard + dir + File.separator)
		return file.exists() || file.mkdir()
	}

	private fun Share(context: Context, file: File)
	{
		val share = Intent(Intent.ACTION_SEND)
		share.type = "*/*"
		val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			FileProvider.getUriForFile(context, context.getString(R.string.authorities), file)
		else
			Uri.fromFile(file)
		Logs.i(TAG, "Share: " + uri)
		share.putExtra(Intent.EXTRA_STREAM, uri)
		grantUriPermission(context, share, uri)
		context.startActivity(Intent.createChooser(share, "分享" + file.name + "到"))
	}

	private fun Share(context: Context, uriList: ArrayList<Uri>)
	{
		val share = Intent(Intent.ACTION_SEND_MULTIPLE)
		share.type = "*/*"
		share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
		context.startActivity(Intent.createChooser(share, "分享到"))
	}

	fun doShare(context: Context, files: ArrayList<File>)
	{
		val uriList = ArrayList<Uri>()
		files.forEach {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				uriList.add(FileProvider.getUriForFile(context, context.getString(R.string.authorities), it))
			else
				uriList.add(Uri.fromFile(it))
		}
		Share(context, uriList)
	}

	fun grantUriPermission(context: Context, intent: Intent, uri: Uri)
	{
		val list = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
		list.forEach {
			context.grantUriPermission(it.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
	}

	fun doShare(context: Context, format: CustomFormat, installApp: InstallApp, dir: String,
				extensionName: String)
	{
		doShare(context, format.toFormat(installApp) + "." + extensionName, dir)
	}

	fun doShare(context: Context, installApp: InstallApp, dir: String, extensionName: String)
	{
		doShare(context, installApp.name + "_" + installApp.versionName + "." + extensionName, dir)
	}

	fun doShare(context: Context, fileName: String, dir: String)
	{
		val file = File(Environment.getExternalStoragePublicDirectory(dir), fileName)
		Share(context, file)
	}

	fun saveDrawableToSd(drawable: Drawable, path: String): Boolean
	{
		try
		{
			val file = File(path)
			if (!file.parentFile.exists())
			{
				file.parentFile.mkdirs()
			}
			val out = FileOutputStream(file)
			val bitmap: Bitmap
			when
			{
				(drawable is BitmapDrawable) -> bitmap = drawable.bitmap
				(drawable is VectorDrawableCompat) -> bitmap = getBitmap(drawable)
				(drawable is VectorDrawable) -> bitmap = getBitmap(drawable)
				else ->
				{
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable)
					{
						bitmap = getBitmap(drawable)
					}
					else
					{
						Logs.i(TAG, "saveDrawableToSd: SDK版本低于26或者不支持的drawable")
						return false
					}
				}
			}
			bitmap.compress(Bitmap.CompressFormat.PNG, 10, out)
			out.close()
		}
		catch (e: IOException)
		{
			e.printStackTrace()
			return false
		}
		return true
	}

	private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap
	{
		val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
				vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
		vectorDrawable.draw(canvas)
		return bitmap
	}

	private fun getBitmap(vectorDrawableCompat: VectorDrawableCompat): Bitmap
	{
		val bitmap = Bitmap.createBitmap(vectorDrawableCompat.intrinsicWidth,
				vectorDrawableCompat.intrinsicHeight, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		vectorDrawableCompat.setBounds(0, 0, canvas.width, canvas.height)
		vectorDrawableCompat.draw(canvas)
		return bitmap
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun getBitmap(adaptiveIconDrawable: AdaptiveIconDrawable): Bitmap
	{
		val layerDrawable = LayerDrawable(arrayOf(adaptiveIconDrawable.background, adaptiveIconDrawable.foreground))
		val bitmap = Bitmap.createBitmap(layerDrawable.intrinsicWidth, layerDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		layerDrawable.setBounds(0, 0, canvas.width, canvas.height)
		layerDrawable.draw(canvas)
		return bitmap
	}

	private fun getFileEnd(path: String?): String
	{
		return path!!.substring(path.lastIndexOf(".") + 1)
	}

	fun saveList(context: Context, list: List<InstallApp>, fileName: String)
	{
		val file = File(context.externalCacheDir!!.absolutePath + File.separator + fileName)
		try
		{
			if (file.exists() || file.createNewFile())
			{
				val fileOutputStream = FileOutputStream(file)
				val objectOutputStream = ObjectOutputStream(fileOutputStream)
				objectOutputStream.writeObject(list)
				fileOutputStream.close()
				objectOutputStream.close()
			}
		}
		catch (e: Exception)
		{
			if (file.exists())
				file.delete()
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun getList(context: Context, fileName: String): List<InstallApp>?
	{
		var savedArrayList: ArrayList<InstallApp>? = null
		try
		{
			val file = File(context.externalCacheDir!!.absolutePath + File.separator + fileName)
			if (file.exists() || file.createNewFile())
			{
				val fileInputStream = FileInputStream(file.toString())
				val objectInputStream = ObjectInputStream(fileInputStream)
				savedArrayList = objectInputStream.readObject() as ArrayList<InstallApp>
				fileInputStream.close()
				objectInputStream.close()
			}
		}
		catch (ignore: Exception)
		{
		}
		return savedArrayList
	}

	fun isCacheAvailable(context: Context): Boolean
	{
		val dir = File(context.externalCacheDir!!.absolutePath + File.separator)
		val files = dir.listFiles()
		var temp = 0
		files.forEach {
			val now = Calendar.getInstance().timeInMillis
			val modified = it.lastModified()
			if (now - modified >= 3 * 86400000)
			{
				temp++
				it.delete()
			}
		}
		return temp == 0
	}

	fun checkObb(packageName: String): ArrayList<File>?
	{
		val dir = File(Environment.getExternalStoragePublicDirectory("Android").absolutePath + File.separator + "obb" + File.separator + packageName + File.separator)
		Logs.i(TAG, "checkObb: " + dir.absolutePath)
		if (!dir.exists() || !dir.isDirectory)
			return null
		val list = ArrayList<File>()
		dir.listFiles()
				.forEach {
					if (getFileEnd(it.absolutePath).toLowerCase() == "obb")
						list.add(it)
				}
		return list
	}

	fun getApkIconPath(context: Context, apkPath: String?): String
	{
		val packageManager = context.packageManager
		val packageInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES)
		val path: String
		if (packageInfo != null)
		{
			val applicationInfo = packageInfo.applicationInfo
			applicationInfo.sourceDir = apkPath
			applicationInfo.publicSourceDir = apkPath
			path = context.cacheDir.absolutePath + File.separator + applicationInfo.packageName
			saveDrawableToSd(applicationInfo.loadIcon(packageManager), path)
			return path
		}
		return "null"
	}
}
