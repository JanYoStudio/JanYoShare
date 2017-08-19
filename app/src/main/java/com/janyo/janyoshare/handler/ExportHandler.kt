package com.janyo.janyoshare.handler

import android.app.Activity
import android.os.Handler
import android.os.Message
import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter


class ExportHandler(private val adapter: AppRecyclerViewAdapter,
					private var activity: Activity) : Handler()
{
	override fun handleMessage(msg: Message?)
	{
		adapter.multiChoiceList.clear()
		adapter.notifyDataSetChanged()
		activity.invalidateOptionsMenu()
	}
}