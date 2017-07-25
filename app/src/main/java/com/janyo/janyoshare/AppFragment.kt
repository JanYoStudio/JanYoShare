@file:Suppress("DEPRECATION")

package com.janyo.janyoshare

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*

import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.util.AppManager
import com.janyo.janyoshare.util.JYFileUtil
import com.janyo.janyoshare.util.Settings

import java.util.ArrayList

class AppFragment : Fragment()
{
	private var coordinatorLayout: CoordinatorLayout? = null
	private var swipeRefreshLayout: SwipeRefreshLayout? = null
	private var appRecyclerViewAdapter: AppRecyclerViewAdapter? = null
	private val installAppList = ArrayList<InstallApp>()
	private val showList = ArrayList<InstallApp>()
	private var type = -1
	private var settings: Settings? = null
	private var index = 0
	private var loadHandler: LoadHandler? = null
	private var renameHandler: RenameHandler? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		type = arguments.getInt("type")
		settings = Settings(activity)
		index = settings!!.sort
		setHasOptionsMenu(true)
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
	{
		inflater.inflate(R.menu.menu_main, menu)
		@Suppress("CAST_NEVER_SUCCEEDS")
		val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
		val searchView = menu.findItem(R.id.action_search).actionView as SearchView
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
					val searchList = AppManager.searchApps(installAppList, query)
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
					val searchList = AppManager.searchApps(installAppList, newText)
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
	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		when (item.itemId)
		{
			R.id.action_clear -> Snackbar.make(activity.findViewById<View>(R.id.coordinatorLayout), String.format(getString(R.string.hint_clear_file), (if (JYFileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败")), Snackbar.LENGTH_SHORT)
					.show()
			R.id.action_sort ->
			{
				index = settings!!.sort
				AlertDialog.Builder(activity)
						.setTitle(R.string.hint_select_sort)
						.setSingleChoiceItems(R.array.sort, index) { _, i -> index = i }
						.setPositiveButton(R.string.action_done) { _, _ ->
							settings!!.sort = index
							swipeRefreshLayout!!.isRefreshing = true
							refresh()
						}
						.show()
			}
			R.id.action_settings -> startActivity(Intent(activity, FileTransferConfigureActivity::class.java))
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
							  savedInstanceState: Bundle?): View?
	{
		coordinatorLayout = activity.findViewById(R.id.coordinatorLayout)
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
		loadHandler = LoadHandler(showList, installAppList, appRecyclerViewAdapter!!, swipeRefreshLayout!!)
		renameHandler = RenameHandler(activity)

		swipeRefreshLayout!!.isRefreshing = true
		refresh()

		swipeRefreshLayout!!.setOnRefreshListener { refresh() }
		return view
	}

	fun refreshList()
	{
		index = settings!!.sort
		swipeRefreshLayout!!.isRefreshing = true
		refresh()
	}

	fun refresh()
	{
		Thread(Runnable {
			val installAppList = AppManager.getInstallAppList(activity, type, index)
			val message = Message()
			message.obj = installAppList
			message.what = 1
			loadHandler!!.sendMessage(message)
		}).start()
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

internal class LoadHandler(private val showList: MutableList<InstallApp>,
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

internal class RenameHandler(private val activity: Activity) : Handler()
{
	override fun handleMessage(message: Message)
	{
		when (message.what)
		{
			1 ->
			{
				val installApp = message.obj as InstallApp
				val coordinatorLayout: CoordinatorLayout = activity.findViewById(R.id.coordinatorLayout)
				val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit, TextInputLayout(activity), false)
				val text: TextInputLayout = view.findViewById(R.id.layout)
				text.hint = installApp.name + "_" + installApp.versionName
				AlertDialog.Builder(activity)
						.setTitle(R.string.hint_new_name)
						.setView(view)
						.setPositiveButton(R.string.action_done, { _, _ ->
							if (JYFileUtil.fileRename(installApp.name!!, installApp.versionName!!, activity.getString(R.string.app_name), text.editText!!.text.toString()))
							{
								JYFileUtil.doShare(activity, text.editText!!.text.toString(), activity.getString(R.string.app_name))
							}
							else
							{
								Snackbar.make(coordinatorLayout, R.string.hint_rename_error, Snackbar.LENGTH_SHORT)
										.show()
							}
						})
						.show()
			}
		}
	}
}