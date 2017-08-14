package com.janyo.janyoshare.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationCompat

import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile

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
		val mimeType = context.contentResolver.getType(Uri.parse(transferFile.fileUri))
		val intent = Intent(Intent.ACTION_VIEW)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		intent.setDataAndType(Uri.parse(transferFile.fileUri), mimeType)
		val builder = NotificationCompat.Builder(context, context.getString(R.string.app_name))
				.setSmallIcon(R.drawable.ic_send)
				.setContentTitle(title)
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
