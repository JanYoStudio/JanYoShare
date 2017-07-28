package com.janyo.janyoshare.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Environment
import android.support.graphics.drawable.VectorDrawableCompat
import com.janyo.janyoshare.classes.InstallApp
import com.mystery0.tools.Logs.Logs
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

object JYFileUtil
{
	private val TAG = "JYFileUtil"

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

	fun getFilePath(fileName: String, version: String, dir: String): String
	{
		return File(Environment.getExternalStoragePublicDirectory(dir), fileName).absolutePath + "_" + version + ".apk"
	}

	fun fileToSD(inputPath: String, fileName: String, version: String, dir: String): Int
	{
		val outFile = File(Environment.getExternalStoragePublicDirectory(dir), fileName)
		return fileCopy(inputPath, outFile.absolutePath + "_" + version + ".apk")
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

	fun fileRename(name: String, versionName: String, dir: String, newName: String): Boolean
	{
		val file = File(Environment.getExternalStoragePublicDirectory(dir), name + "_" + versionName + ".apk")
		return file.renameTo(File(Environment.getExternalStoragePublicDirectory(dir), newName + ".apk"))
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
		share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
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
			uriList.add(Uri.fromFile(it))
		}
		Share(context, uriList)
	}

	fun doShare(context: Context, name: String, versionName: String, dir: String)
	{
		doShare(context, name + "_" + versionName + ".apk", dir)
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
			val out = FileOutputStream(file)
			val bitmap: Bitmap
			when
			{
				(drawable is BitmapDrawable) -> bitmap = drawable.bitmap
				(drawable is VectorDrawableCompat) -> bitmap = getBitmap(drawable)
				(drawable is VectorDrawable) -> bitmap = getBitmap(drawable)
				else ->
				{
					Logs.i(TAG, "saveDrawableToSd: 不支持的drawable类型")
					return false
				}
			}
			bitmap.compress(Bitmap.CompressFormat.PNG, 50, out)
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

	fun getFileEnd(path: String?): String
	{
		return path!!.substring(path.lastIndexOf(".") + 1)
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
			e.printStackTrace()
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
		catch (e: Exception)
		{
			e.printStackTrace()
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
				.filter { it.endsWith(".obb") }
				.forEach { list.add(it) }
		return list
	}
}
