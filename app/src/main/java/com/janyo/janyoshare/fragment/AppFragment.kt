package com.janyo.janyoshare.fragment

import android.os.Bundle
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.janyo.janyoshare.APP
import com.janyo.janyoshare.R
import com.janyo.janyoshare.activity.MainActivity

import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter
import com.janyo.janyoshare.`interface`.ExportListener
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.handler.ExportHandler
import com.janyo.janyoshare.handler.LoadHandler
import com.janyo.janyoshare.util.*
import vip.mystery0.tools.Logs.Logs
import java.io.File

import java.util.ArrayList
import java.util.concurrent.Executors

class AppFragment : Fragment()
{
	private lateinit var coordinatorLayout: CoordinatorLayout
	private lateinit var loadHandler: LoadHandler
	private val TAG = "AppFragment"
	private val singleThreadPool = Executors.newSingleThreadExecutor()
	private var type = -1
	private var settings = Settings.getInstance(APP.getInstance())
	private var index = 0
	private var isReadyTag = false
	private lateinit var swipeRefreshLayout: SwipeRefreshLayout
	lateinit var appRecyclerViewAdapter: AppRecyclerViewAdapter
	lateinit var exportHandler: ExportHandler
	val installAppList = ArrayList<InstallApp>()
	val showList = ArrayList<InstallApp>()

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		type = arguments.getInt("type")
		Logs.i(TAG, "onCreate: 创建app的fragment" + type)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View?
	{
		Logs.i(TAG, "onCreateView: 创建视图")
		coordinatorLayout = activity.findViewById(R.id.coordinatorLayout)
		val view = inflater.inflate(R.layout.fragment_app, container, false)
		val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
		swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light)

		appRecyclerViewAdapter = AppRecyclerViewAdapter(activity, showList)
		recyclerView.layoutManager = LinearLayoutManager(activity)
		recyclerView.adapter = appRecyclerViewAdapter
		loadHandler = LoadHandler(showList, installAppList, appRecyclerViewAdapter, swipeRefreshLayout)
		exportHandler = ExportHandler(appRecyclerViewAdapter, activity)
		swipeRefreshLayout.setOnRefreshListener { singleThreadPool.execute { refresh() } }
		isReadyTag = true
		return view
	}

	fun clearSelected(activity: MainActivity)
	{
		singleThreadPool.execute {
			while (true)
			{
				if (isReadyTag)
					break
				Logs.i(TAG, "clearSelected: 等待初始化")
				Thread.sleep(200)
			}
			if (appRecyclerViewAdapter.multiChoiceList.size != 0)
			{
				appRecyclerViewAdapter.multiChoiceList.clear()
				activity.invalidateOptionsMenu()
			}
		}
	}

	fun exportAPK(list: List<InstallApp>, listener: ExportListener)
	{
		Logs.i(TAG, "exportAPK: " + list.size)
		Thread(Runnable {
			val fileList = ArrayList<File>()
			val cacheThreadPool = Executors.newCachedThreadPool()
			var finish = 0
			var error = 0
			list.forEach {
				cacheThreadPool.submit {
					val code: Int = if (settings.customFileName.format == "")
						JYFileUtil.fileToSD(it.sourceDir!!, it, getString(R.string.app_name), "apk")
					else
						JYFileUtil.fileToSD(it.sourceDir!!, settings.customFileName, it, getString(R.string.app_name), "apk")
					if (code == -1)
					{
						Toast.makeText(activity, getString(R.string.hint_copy_error_with_name, it.name), Toast.LENGTH_SHORT)
								.show()
						error++
					}
					else
					{
						if (settings.customFileName.format == "")
							fileList.add(JYFileUtil.getFilePath(it, context.getString(R.string.app_name), "apk"))
						else
							fileList.add(JYFileUtil.getFilePath(settings.customFileName, it, context.getString(R.string.app_name), "apk"))
						finish++
					}
				}
			}
			cacheThreadPool.shutdown()
			while (true)
			{
				if (cacheThreadPool.isTerminated)
				{
					listener.done(finish, error, fileList)
					break
				}
				Thread.sleep(100)
			}
		}).start()
	}

	fun refreshList()
	{
		singleThreadPool.execute {
			while (true)
			{
				if (isReadyTag)
					break
				Logs.i(TAG, "refreshList: 等待初始化")
				Thread.sleep(200)
			}
			val message = Message()
			message.what = LoadHandler.READY_TO_REFRESH
			loadHandler.sendMessage(message)
			index = settings.sort
			if (settings.savedSort == settings.sort && JYFileUtil.isCacheAvailable(activity))
			{
				getCatchList()
			}
			else
			{
				refresh()
			}
		}
	}

	private fun getCatchList()
	{
		var fileName = ""
		when (type)
		{
			AppManager.SYSTEM -> fileName = "system.list"
			AppManager.USER -> fileName = "user.list"
		}
		val installAppList = JYFileUtil.getList(context, fileName)
		if (installAppList != null)
		{
			val message = Message()
			message.obj = installAppList
			message.what = LoadHandler.REFRESH_DONE
			loadHandler.sendMessage(message)
		}
		else
		{
			refresh()
		}
	}

	private fun refresh()
	{
		index = settings.sort
		val installAppList = AppManager.getInstallAppList(activity, type, index, true)
		val message = Message()
		message.obj = installAppList
		message.what = LoadHandler.REFRESH_DONE
		loadHandler.sendMessage(message)
		when (type)
		{
			AppManager.SYSTEM -> JYFileUtil.saveList(activity, installAppList, "system.list")
			AppManager.USER -> JYFileUtil.saveList(activity, installAppList, "user.list")
		}
		settings.savedSort = index
	}

	companion object
	{
		fun newInstance(type: Int): AppFragment
		{
			val bundle = Bundle()
			bundle.putInt("type", type)
			val fragment = AppFragment()
			fragment.arguments = bundle
			return fragment
		}
	}
}
