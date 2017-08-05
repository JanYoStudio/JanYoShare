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
	fun notify(context: Context, number: Int, action: String?)
	{
		val title = context.getString(R.string.hint_transfer_file_notification_title, FileTransferHandler.getInstance().currentFile!!.fileName)

		val builder = NotificationCompat.Builder(context, context.getString(R.string.app_name))
				.setSmallIcon(R.drawable.ic_send)
				.setContentTitle(title)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setNumber(number)
				.setProgress(100, FileTransferHandler.getInstance().currentProgress, false)
				.setAutoCancel(true)
		notify(context, builder.build(), false)
	}

	fun done(context: Context, number: Int, transferFile: TransferFile)
	{
		val title = context.getString(R.string.hint_transfer_file_notification_done, FileTransferHandler.getInstance().currentFile!!.fileName)
		val mimeType = context.contentResolver.getType(Uri.parse(transferFile.fileUri))
		val intent = Intent(Intent.ACTION_VIEW)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		intent.setDataAndType(Uri.parse(transferFile.fileUri), mimeType)
		val builder = NotificationCompat.Builder(context, context.getString(R.string.app_name))
				.setSmallIcon(R.drawable.ic_send)
				.setContentTitle(title)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setNumber(number)
				.setContentIntent(
						PendingIntent.getActivity(
								context,
								0,
								intent,
								PendingIntent.FLAG_UPDATE_CURRENT))
				.setAutoCancel(true)

		notify(context, builder.build(), true)
	}

	private fun notify(context: Context, notification: Notification, cancelable: Boolean)
	{
		if (!cancelable)
			notification.flags = Notification.FLAG_NO_CLEAR
		val notificationManager = context
				.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.notify(FileTransferHandler.getInstance().currentFile!!.fileName, 0, notification)
	}

	fun cancel(context: Context)
	{
		val notificationManager = context
				.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(FileTransferHandler.getInstance().currentFile!!.fileName, 0)
	}
}
