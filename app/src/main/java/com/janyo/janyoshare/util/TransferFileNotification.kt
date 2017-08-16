package com.janyo.janyoshare.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.FileProvider

import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile
import java.io.File

object TransferFileNotification
{
	fun notify(context: Context, id: Int, action: String?)
	{
		val index = FileTransferHelper.getInstance().currentFileIndex
		val transferFile = FileTransferHelper.getInstance().fileList[index]
		val title = context.getString(R.string.hint_transfer_file_notification_title, transferFile.fileName)

		val builder = NotificationCompat.Builder(context, context.getString(R.string.app_name))
				.setSmallIcon(R.drawable.ic_send)
				.setContentTitle(title)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setProgress(100, transferFile.transferProgress, false)
				.setAutoCancel(true)
		notify(context, id, builder.build())
	}

	fun done(context: Context, id: Int, transferFile: TransferFile)
	{
		val title = context.getString(R.string.hint_transfer_file_notification_done, transferFile.fileName)
		var path = ""
		when (FileTransferHelper.getInstance().tag)
		{
			1 ->
			{
				path = transferFile.filePath!!
			}
			2 ->
			{
				path = JYFileUtil.getSaveFilePath(transferFile.fileName!!, "JY Share")
			}
		}
		val uri: Uri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
			FileProvider.getUriForFile(context, context.getString(R.string.authorities), File(path))
		else
			Uri.fromFile(File(path))
		val mimeType = context.contentResolver.getType(uri)
		val intent = Intent(Intent.ACTION_VIEW)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		intent.setDataAndType(uri, mimeType)
		JYFileUtil.grantUriPermission(context, intent, uri)
		val builder = NotificationCompat.Builder(context, context.getString(R.string.app_name))
				.setSmallIcon(R.drawable.ic_send)
				.setContentTitle(title)
				.setContentText(context.getString(R.string.hint_transfer_file_notification_message))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setContentIntent(
						PendingIntent.getActivity(
								context,
								0,
								intent,
								PendingIntent.FLAG_UPDATE_CURRENT))
				.setAutoCancel(true)

		notify(context, id, builder.build())
	}

	private fun notify(context: Context, id: Int, notification: Notification)
	{
		val index = FileTransferHelper.getInstance().currentFileIndex
		val transferFile = FileTransferHelper.getInstance().fileList[index]
		val notificationManager = context
				.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.notify(transferFile.fileName, id, notification)
	}

	fun cancel(context: Context, id: Int)
	{
		val index = FileTransferHelper.getInstance().currentFileIndex
		val transferFile = FileTransferHelper.getInstance().fileList[index]
		val notificationManager = context
				.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(transferFile.fileName, id)
	}
}
