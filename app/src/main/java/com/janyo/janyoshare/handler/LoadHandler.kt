package com.janyo.janyoshare.handler

import android.os.Handler
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter
import com.janyo.janyoshare.classes.InstallApp
import com.mystery0.tools.Logs.Logs

class LoadHandler(private val showList: MutableList<InstallApp>,
				  private val installAppList: MutableList<InstallApp>,
				  private val appRecyclerViewAdapter: AppRecyclerViewAdapter,
				  private val swipeRefreshLayout: SwipeRefreshLayout) : Handler()
{
	private val TAG = "LoadHandler"

	@Suppress("UNCHECKED_CAST")
	override fun handleMessage(message: Message)
	{
		when (message.what)
		{
			READY_TO_REFRESH ->
			{
				Logs.i(TAG, "handleMessage: 准备刷新")
				swipeRefreshLayout.isRefreshing = true
			}
			REFRESH_DONE ->
			{
				Logs.i(TAG, "handleMessage: 刷新完毕")
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

	companion object
	{
		val READY_TO_REFRESH = 1
		val REFRESH_DONE = 2
	}
}