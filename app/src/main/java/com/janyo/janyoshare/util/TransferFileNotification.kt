package com.janyo.janyoshare.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat

import com.janyo.janyoshare.R
import com.janyo.janyoshare.activity.FileTransferActivity
import com.janyo.janyoshare.service.TransferCancelService
import com.janyo.janyoshare.service.TransferPauseService
import com.janyo.janyoshare.service.TransferResumeService

object TransferFileNotification
{
	private val NOTIFICATION_TAG = "TransferFile"

	fun notify(context: Context, number: Int, action: String?)
	{
		val title = context.getString(R.string.hint_transfer_file_notification_title, FileTransferHandler.getInstance().currentFile!!.fileName)

		val builder = NotificationCompat.Builder(context, context.getString(R.string.app_name))
				.setSmallIcon(R.drawable.ic_send)
				.setContentTitle(title)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setNumber(number)
				.setProgress(100, FileTransferHandler.getInstance().currentProgress, false)
//				.setContentIntent(
//						PendingIntent.getActivity(
//								context,
//								0,
//								Intent(context, FileTransferActivity::class.java),
//								PendingIntent.FLAG_UPDATE_CURRENT))
				.setAutoCancel(true)

//		val pauseIntent = Intent(context, TransferPauseService::class.java)
//		val resumeIntent = Intent(context, TransferResumeService::class.java)
//		val cancelIntent = Intent(context, TransferCancelService::class.java)

//		when (action)
//		{
//			"start", "resume" ->
//				builder.addAction(
//						R.drawable.ic_pause,
//						context.resources.getString(R.string.action_pause),
//						PendingIntent.getService(
//								context,
//								0,
//								pauseIntent,
//								PendingIntent.FLAG_UPDATE_CURRENT))
//						.addAction(
//								R.drawable.ic_cancel,
//								context.resources.getString(R.string.action_cancel),
//								PendingIntent.getService(
//										context,
//										0,
//										cancelIntent,
//										PendingIntent.FLAG_UPDATE_CURRENT))
//			"pause" ->
//				builder.addAction(
//						R.drawable.ic_resume,
//						context.resources.getString(R.string.action_resume),
//						PendingIntent.getService(
//								context,
//								0,
//								resumeIntent,
//								PendingIntent.FLAG_UPDATE_CURRENT))
//						.addAction(
//								R.drawable.ic_cancel,
//								context.resources.getString(R.string.action_cancel),
//								PendingIntent.getService(
//										context,
//										0,
//										cancelIntent,
//										PendingIntent.FLAG_UPDATE_CURRENT))
//		}

		notify(context, builder.build())
	}

	fun done(context: Context, number: Int)
	{
		val title = context.getString(R.string.hint_transfer_file_notification_done, FileTransferHandler.getInstance().currentFile!!.fileName)

		val builder = NotificationCompat.Builder(context, context.getString(R.string.app_name))
				.setSmallIcon(R.drawable.ic_send)
				.setContentTitle(title)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setNumber(number)
//				.setContentIntent(
//						PendingIntent.getActivity(
//								context,
//								0,
//								Intent(context, FileTransferActivity::class.java),
//								PendingIntent.FLAG_UPDATE_CURRENT))
				.setAutoCancel(true)

		notify(context, builder.build())
	}

	private fun notify(context: Context, notification: Notification)
	{
		notification.flags = Notification.FLAG_NO_CLEAR
		val notificationManager = context
				.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.notify(NOTIFICATION_TAG, 0, notification)
	}

	fun cancel(context: Context)
	{
		val notificationManager = context
				.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(NOTIFICATION_TAG, 0)
	}
}
