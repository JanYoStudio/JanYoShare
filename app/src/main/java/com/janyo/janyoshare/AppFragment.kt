package com.janyo.janyoshare

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.Toast

import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.handler.ExportHandler
import com.janyo.janyoshare.handler.LoadHandler
import com.janyo.janyoshare.util.*
import dmax.dialog.SpotsDialog
import java.io.File

import java.util.ArrayList
import java.util.concurrent.Executors

class AppFragment : Fragment()
{
	private lateinit var coordinatorLayout: CoordinatorLayout
	private lateinit var swipeRefreshLayout: SwipeRefreshLayout
	private lateinit var appRecyclerViewAdapter: AppRecyclerViewAdapter
	private val installAppList = ArrayList<InstallApp>()
	private val showList = ArrayList<InstallApp>()
	private var type = -1
	private lateinit var settings: Settings
	private var index = 0
	private lateinit var loadHandler: LoadHandler
	private lateinit var exportHandler: ExportHandler
	private val singleThreadPool = Executors.newSingleThreadExecutor()

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		type = arguments.getInt("type")
		settings = Settings.getInstance(activity)
		setHasOptionsMenu(true)
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
	{
		inflater.inflate(R.menu.menu_main, menu)
		appRecyclerViewAdapter.menu = menu
		@Suppress("CAST_NEVER_SUCCEEDS")
		val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
		val searchView = menu.findItem(R.id.action_search).actionView as SearchView
		searchView.setOnQueryTextFocusChangeListener { _, b ->
			val action_clear = menu.findItem(R.id.action_clear)
			val action_sort = menu.findItem(R.id.action_sort)
			if (b)
			{
				action_clear.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
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
					val searchList = AppManager.searchApps(installAppList, query)
					showList.addAll(searchList)
				}
				else
				{
					showList.addAll(installAppList)
				}
				appRecyclerViewAdapter.notifyDataSetChanged()
				return true
			}

			override fun onQueryTextChange(newText: String): Boolean
			{
				showList.clear()
				if (newText.isNotEmpty())
				{
					val searchList = AppManager.searchApps(installAppList, newText)
					showList.addAll(searchList)
				}
				else
				{
					showList.addAll(installAppList)
				}
				appRecyclerViewAdapter.notifyDataSetChanged()
				return false
			}
		})
	}

	@SuppressLint("InflateParams")
	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		when (item.itemId)
		{
			android.R.id.home ->
			{
				val drawer: DrawerLayout = activity.findViewById(R.id.drawer_layout)
				drawer.openDrawer(GravityCompat.START)
			}
			R.id.action_clear -> Snackbar.make(activity.findViewById<View>(R.id.coordinatorLayout), String.format(getString(R.string.hint_clear_file), (if (JYFileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败")), Snackbar.LENGTH_SHORT)
					.show()
			R.id.action_sort ->
			{
				index = settings.sort
				AlertDialog.Builder(activity)
						.setTitle(R.string.hint_select_sort)
						.setSingleChoiceItems(R.array.sort, index) { _, i -> index = i }
						.setPositiveButton(R.string.action_done) { _, _ ->
							settings.sort = index
							swipeRefreshLayout.isRefreshing = true
							refresh()
						}
						.show()
			}
			R.id.action_export ->
			{
				val list = appRecyclerViewAdapter.multiChoiceList
				val progressDialog = SpotsDialog(activity, R.style.SpotsDialog)
				progressDialog.setCancelable(false)
				progressDialog.setMessage(getString(R.string.copy_file_loading))
				progressDialog.show()
				exportAPK(list, object : ExportListener
				{
					override fun done(finish: Int, error: Int, fileList: ArrayList<File>)
					{
						progressDialog.dismiss()
						Snackbar.make(coordinatorLayout, getString(R.string.hint_copy_done_with_number, finish, error), Snackbar.LENGTH_SHORT)
								.show()
						exportHandler.sendEmptyMessage(0)
					}
				})
			}
			R.id.action_send ->
			{
				val list = appRecyclerViewAdapter.multiChoiceList
				val progressDialog = SpotsDialog(activity, R.style.SpotsDialog)
				progressDialog.setCancelable(false)
				progressDialog.setMessage(getString(R.string.copy_file_loading))
				progressDialog.show()
				exportAPK(list, object : ExportListener
				{
					override fun done(finish: Int, error: Int, fileList: ArrayList<File>)
					{
						progressDialog.dismiss()
						Snackbar.make(coordinatorLayout, getString(R.string.hint_copy_done_with_number, finish, error), Snackbar.LENGTH_SHORT)
								.addCallback(object : Snackbar.Callback()
								{
									override fun onDismissed(transientBottomBar: Snackbar?,
															 event: Int)
									{
										JYFileUtil.doShare(context, fileList)
										exportHandler.sendEmptyMessage(0)
									}
								})
								.show()
					}
				})
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
							  savedInstanceState: Bundle?): View?
	{
		coordinatorLayout = activity.findViewById(R.id.coordinatorLayout)
		val view = inflater!!.inflate(R.layout.fragment_app, container, false)
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

		refreshList()

		swipeRefreshLayout.setOnRefreshListener { refresh() }
		return view
	}

	private fun exportAPK(list: List<InstallApp>, listener: ExportListener)
	{
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
		index = settings.sort
		swipeRefreshLayout.isRefreshing = true
		if (JYFileUtil.isCacheAvailable(context))
		{
			getCatchList()
		}
		else
		{
			refresh()
		}
	}

	private fun getCatchList()
	{
		Thread(Runnable {
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
				message.what = 1
				loadHandler.sendMessage(message)
			}
			else
			{
				refresh()
			}
		}).start()
	}

	private fun refresh()
	{
		singleThreadPool.execute {
			val installAppList = AppManager.getInstallAppList(context, type, index, true)
			when (type)
			{
				AppManager.SYSTEM -> JYFileUtil.saveList(context, installAppList, "system.list")
				AppManager.USER -> JYFileUtil.saveList(context, installAppList, "user.list")
			}
			val message = Message()
			message.obj = installAppList
			message.what = 1
			loadHandler.sendMessage(message)
		}
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

	interface ExportListener
	{
		fun done(finish: Int, error: Int, fileList: ArrayList<File>)
	}
}
