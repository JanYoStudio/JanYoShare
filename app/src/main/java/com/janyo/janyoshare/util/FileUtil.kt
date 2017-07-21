package com.janyo.janyoshare.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.mystery0.tools.FileUtil.FileUtil
import com.mystery0.tools.Logs.Logs

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object FileUtil
{
	private val TAG = "FileUtil"

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
		Logs.i(TAG, "deleteFile: 该文件不是文件")
		return true
	}

	fun fileToSD(inputPath: String, fileName: String, version: String, dir: String): Int
	{
		val outFile = File(Environment.getExternalStoragePublicDirectory(dir), fileName)
		return fileCopy(inputPath, outFile.toString() + "_" + version + ".apk")
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
			val Buff = ByteArray(1024)
			var ReadCount = 0
			while (ReadCount != -1)
			{
				fileOutputStream.write(Buff, 0, ReadCount)
				ReadCount = fileInputStream.read(Buff)
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
		context.startActivity(Intent.createChooser(share, "分享"))
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

	fun FormatFileSize(fileSize: Long): String
	{
		return FileUtil.FormatFileSize(fileSize)
	}
}
