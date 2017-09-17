package com.janyo.janyoshare.handler

import android.os.Handler
import android.os.Message
import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter

/**
 * Created by myste.
 */
class SearchHandler(private val appRecyclerViewAdapter: AppRecyclerViewAdapter) : Handler()
{
	override fun handleMessage(msg: Message?)
	{
		appRecyclerViewAdapter.notifyDataSetChanged()
	}
}