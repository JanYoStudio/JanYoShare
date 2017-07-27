package com.janyo.janyoshare.handler

import android.os.Handler
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter
import com.janyo.janyoshare.classes.InstallApp

class LoadHandler(private val showList: MutableList<InstallApp>,
				  private val installAppList: MutableList<InstallApp>,
				  private val appRecyclerViewAdapter: AppRecyclerViewAdapter,
				  private val swipeRefreshLayout: SwipeRefreshLayout) : Handler()
{

	@Suppress("UNCHECKED_CAST")
	override fun handleMessage(message: Message)
	{
		when (message.what)
		{
			1 ->
			{
				val installApps = message.obj as List<InstallApp>
				showList.clear()
				showList.addAll(installApps)
				installAppList.clear()
				installAppList.addAll(installApps)
				appRecyclerViewAdapter.notifyDataSetChanged()
				swipeRefreshLayout.isRefreshing = false
			}
		}
	}
}