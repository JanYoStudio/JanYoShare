package com.janyo.janyoshare

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.util.AppManager
import com.janyo.janyoshare.util.FileUtil
import com.janyo.janyoshare.util.Settings

import java.util.ArrayList

class AppFragment : Fragment()
{
	private var swipeRefreshLayout: SwipeRefreshLayout? = null
	private var appRecyclerViewAdapter: AppRecyclerViewAdapter? = null
	private val installAppList = ArrayList<InstallApp>()
	private val showList = ArrayList<InstallApp>()
	private var appManager: AppManager? = null
	private var type: AppManager.AppType? = null
	private var settings: Settings? = null
	private var index = 0
	private var handler: MyHandler? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		type = arguments.getSerializable("type") as AppManager.AppType
		settings = Settings(activity)
		appManager = AppManager(activity)
		index = settings!!.sort
		setHasOptionsMenu(true)
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?)
	{
		inflater!!.inflate(R.menu.menu_main, menu)
		val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
		val searchView = menu!!.findItem(R.id.action_search).actionView as SearchView
		searchView.setOnQueryTextFocusChangeListener { _, b ->
			val action_clear = menu.findItem(R.id.action_clear)
			val action_settings = menu.findItem(R.id.action_settings)
			val action_sort = menu.findItem(R.id.action_sort)
			if (b)
			{
				action_clear.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
				action_settings.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
				action_sort.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
			}
			else
			{
				activity.invalidateOptionsMenu()
			}
		}
		searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
		searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
		{
			override fun onQueryTextSubmit(query: String): Boolean
			{
				showList.clear()
				if (query.isNotEmpty())
				{
					val searchList = appManager!!.searchApps(installAppList, query)
					showList.addAll(searchList)
				}
				else
				{
					showList.addAll(installAppList)
				}
				appRecyclerViewAdapter!!.notifyDataSetChanged()
				return true
			}

			override fun onQueryTextChange(newText: String): Boolean
			{
				showList.clear()
				if (newText.isNotEmpty())
				{
					val searchList = appManager!!.searchApps(installAppList, newText)
					showList.addAll(searchList)
				}
				else
				{
					showList.addAll(installAppList)
				}
				appRecyclerViewAdapter!!.notifyDataSetChanged()
				return false
			}
		})
	}

	@SuppressLint("InflateParams")
	override fun onOptionsItemSelected(item: MenuItem?): Boolean
	{
		when (item!!.itemId)
		{
			R.id.action_clear -> Snackbar.make(activity.findViewById<View>(R.id.coordinatorLayout), "文件清除" + (if (FileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败") + "！", Snackbar.LENGTH_SHORT)
					.show()
			R.id.action_sort ->
			{
				index = settings!!.sort
				AlertDialog.Builder(activity)
						.setTitle("请选择排序方式")
						.setSingleChoiceItems(R.array.sort, index) { _, i -> index = i }
						.setPositiveButton("确定") { _, _ ->
							settings!!.sort = index
							swipeRefreshLayout!!.isRefreshing = true
							refresh()
						}
						.show()
			}
			R.id.action_settings -> startActivity(Intent(activity, SettingsActivity::class.java))
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
							  savedInstanceState: Bundle?): View?
	{
		val view = inflater!!.inflate(R.layout.fragment_app, container, false)
		val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
		swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
		swipeRefreshLayout!!.setColorSchemeResources(
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light)

		appRecyclerViewAdapter = AppRecyclerViewAdapter(activity, showList)
		recyclerView.layoutManager = LinearLayoutManager(activity)
		recyclerView.adapter = appRecyclerViewAdapter
		handler = MyHandler(showList, installAppList, appRecyclerViewAdapter!!, swipeRefreshLayout!!)

		swipeRefreshLayout!!.isRefreshing = true
		refresh()

		swipeRefreshLayout!!.setOnRefreshListener { refresh() }
		return view
	}

	override fun onResume()
	{
		super.onResume()
		index = settings!!.sort
		swipeRefreshLayout!!.isRefreshing = true
		refresh()
	}

	fun refresh()
	{
		Thread(Runnable {
			val installAppList = appManager!!.getInstallAppList(type!!, index)
			val message = Message()
			message.obj = installAppList
			message.what = 1
			handler!!.sendMessage(message)
		}).start()
	}

	companion object
	{

		fun newInstance(type: AppManager.AppType): AppFragment
		{
			val bundle = Bundle()
			bundle.putSerializable("type", type)
			val fragment = AppFragment()
			fragment.arguments = bundle
			return fragment
		}
	}
}

internal class MyHandler(private val showList: MutableList<InstallApp>,
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