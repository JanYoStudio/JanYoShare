package com.janyo.janyoshare.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat

object FileUtil
{
	fun cleanFileDir(dir: String): Boolean
	{
		val file = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + dir + File.separator)
		if (!file.exists())
		{

			file.mkdirs()
		}
		if (file.isDirectory)
		{
			for (item in file.listFiles())
			{

				item.delete()
			}
			return true
		}
		return false
	}

	fun fileToSD(inputPath: String, fileName: String, version: String, dir: String): Int
	{
		val outFile = File(Environment.getExternalStoragePublicDirectory(dir), fileName)
		return fileCopy(inputPath, outFile.toString() + "_" + version + ".apk")
	}

	private fun fileCopy(inputPath: String, outPath: String): Int
	{
		var code = -1
		try
		{
			val fileInputStream = FileInputStream(inputPath)
			val fileOutputStream = FileOutputStream(outPath)
			val Buff = ByteArray(1024)
			var ReadCount=0
			while (ReadCount!=-1)
			{
				fileOutputStream.write(Buff,0,ReadCount)
				ReadCount=fileInputStream.read(Buff)
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
		context.startActivity(Intent.createChooser(share, "分享"))
	}

	fun doShare(context: Context, name: String, versionName: String, dir: String)
	{
		val file = File(Environment.getExternalStoragePublicDirectory(dir), name + "_" + versionName + ".apk")
		Share(context, file)
	}

	fun FormatFileSize(fileSize: Long): String
	{
		val df = DecimalFormat("#.00")
		val fileSizeString: String
		if (fileSize < 1024)
		{
			fileSizeString = df.format(fileSize.toDouble()) + "B"
		}
		else if (fileSize < 1048576)
		{
			fileSizeString = df.format(fileSize.toDouble() / 1024) + "KB"
		}
		else if (fileSize < 1073741824)
		{
			fileSizeString = df.format(fileSize.toDouble() / 1048576) + "MB"
		}
		else
		{
			fileSizeString = df.format(fileSize.toDouble() / 1073741824) + "GB"
		}
		return fileSizeString
	}
}
