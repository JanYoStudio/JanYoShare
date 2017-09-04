package com.janyo.janyoshare.activity

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.janyo.janyoshare.fragment.AppFragment
import com.janyo.janyoshare.R
import com.janyo.janyoshare.adapter.ViewPagerAdapter
import com.janyo.janyoshare.util.AppManager
import com.janyo.janyoshare.util.JYFileUtil
import com.janyo.janyoshare.util.Settings
import com.mystery0.tools.CrashHandler.CrashHandler
import com.mystery0.tools.Logs.Logs
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.File
import com.mystery0.tools.MysteryNetFrameWork.ResponseListener
import com.mystery0.tools.MysteryNetFrameWork.HttpUtil
import com.android.volley.toolbox.Volley
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.gson.Gson
import com.janyo.janyoshare.APP
import com.janyo.janyoshare.callback.ExportListener
import com.janyo.janyoshare.callback.InitGooglePlayListener
import com.janyo.janyoshare.classes.Error
import com.janyo.janyoshare.classes.Response
import com.janyo.janyoshare.handler.PayHandler
import dmax.dialog.SpotsDialog
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener
{
	private lateinit var currentFragment: AppFragment
	private lateinit var img_janyo: ImageView
	private lateinit var payHandler: PayHandler
	private lateinit var spotsDialog: SpotsDialog
	private val TAG = "MainActivity"
	private val PERMISSION_CODE = 233
	private var settings = Settings.getInstance(APP.getInstance())
	private var oneClickTime: Long = 0
	private var isGooglePlayAvailable = true
	private var isGooglePlayPay = false
	var menu: Menu? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		if (settings.dayNight)
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		else
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		super.onCreate(savedInstanceState)
		checkPermission()
		initialization()
		monitor()
	}

	private fun initialization()
	{
		setContentView(R.layout.activity_main)

		spotsDialog = SpotsDialog(this, R.style.SpotsDialog)
		spotsDialog.setCancelable(false)
		spotsDialog.setMessage(getString(R.string.copy_file_loading))

		payHandler = PayHandler(this, Volley.newRequestQueue(this))
		payHandler.initGooglePlay(object : InitGooglePlayListener
		{
			override fun onSuccess()
			{
				isGooglePlayAvailable = true
			}

			override fun onFailed()
			{
				isGooglePlayAvailable = false
			}

		})

		val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
		drawer_layout.addDrawerListener(toggle)
		toggle.syncState()

		img_janyo = nav_view.getHeaderView(0).findViewById(R.id.imageView)
		nav_view.menu.findItem(R.id.action_night).actionView.findViewById<Switch>(R.id.Switch).isChecked = settings.dayNight
		nav_view.menu.findItem(R.id.action_night).actionView.findViewById<Switch>(R.id.Switch).setOnCheckedChangeListener { _, checked ->
			settings.dayNight = checked
			Snackbar.make(coordinatorLayout, R.string.hint_day_night, Snackbar.LENGTH_SHORT)
					.show()
		}

		nav_view.setNavigationItemSelectedListener(this)

		val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
		val userFragment = AppFragment.newInstance(AppManager.USER)
		val systemFragment = AppFragment.newInstance(AppManager.SYSTEM)
		viewPagerAdapter.addFragment(userFragment, getString(R.string.title_fragment_user))
		viewPagerAdapter.addFragment(systemFragment, getString(R.string.title_fragment_system))
		currentFragment = userFragment
		userFragment.refreshList()
		viewpager.adapter = viewPagerAdapter
		title_tabs.setupWithViewPager(viewpager)

		viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
		{
			override fun onPageScrolled(position: Int, positionOffset: Float,
										positionOffsetPixels: Int)
			{
			}

			override fun onPageSelected(position: Int)
			{
				Logs.i(TAG, "onPageSelected: " + position)
				Logs.i(TAG, "onPageSelected: 当前滚动到" + viewPagerAdapter.getPageTitle(position))

				val fragment = viewPagerAdapter.getItem(position) as AppFragment
				fragment.refreshList()
				currentFragment.clearSelected(this@MainActivity)
				currentFragment = fragment
			}

			override fun onPageScrollStateChanged(state: Int)
			{
			}
		})

		JYFileUtil.isDirExist(getString(R.string.app_name))
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && settings.isAutoClean)
		{
			Snackbar.make(coordinatorLayout, String.format(getString(R.string.hint_clear_file), (if (JYFileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败")), Snackbar.LENGTH_SHORT)
					.show()
		}
		CrashHandler.getInstance(this)
				.clean(object : CrashHandler.AutoCleanListener
				{
					override fun done()
					{
						Logs.i(TAG, "done: clean crash log")
					}

					override fun error(message: String?)
					{
						Logs.i(TAG, "error: " + message)
					}
				})
		CrashHandler.getInstance(this)
				.sendException(object : CrashHandler.CatchExceptionListener
				{
					override fun onException(date: String, file: File, appVersionName: String,
											 appVersionCode: Int, AndroidVersion: String,
											 sdk: Int, vendor: String, model: String, ex: Throwable)
					{
						if (settings.isAutoUploadLog)
						{
							val map = HashMap<String, String>()
							val fileMap = HashMap<String, File>()
							fileMap.put("logFile", file)
							map.put("date", date)
							map.put("appName", getString(R.string.app_name))
							map.put("appVersionName", appVersionName)
							map.put("appVersionCode", appVersionCode.toString())
							map.put("androidVersion", AndroidVersion)
							map.put("sdk", sdk.toString())
							map.put("vendor", vendor)
							map.put("model", model)
							HttpUtil(this@MainActivity)
									.setRequestQueue(Volley.newRequestQueue(applicationContext))
									.setUrl("http://123.206.186.70/php/uploadLog/upload_file.php")
									.setRequestMethod(HttpUtil.RequestMethod.POST)
									.setFileRequest(HttpUtil.FileRequest.UPLOAD)
									.isFileRequest(true)
									.setMap(map)
									.setFileMap(fileMap)
									.setResponseListener(object : ResponseListener
									{
										override fun onResponse(code: Int, message: String?)
										{
											val response = Gson().fromJson(message, Response::class.java)
											if (response.code == 0)
											{
												Toast.makeText(applicationContext, R.string.hint_upload_log_done, Toast.LENGTH_SHORT)
														.show()
											}
											else
											{
												Logs.e(TAG, "onResponse: " + message)
											}
										}
									})
									.open()
						}
						else
						{
							val error = Error(date, appVersionName, appVersionCode, AndroidVersion, sdk, vendor, model, ex)
							val bundle = Bundle()
							bundle.putSerializable("file", file)
							bundle.putSerializable("error", error)
							val intent = Intent(this@MainActivity, ErrorActivity::class.java)
							intent.putExtra("error", bundle)
							startActivity(intent)
						}
					}
				})

		setSupportActionBar(toolbar)
		showcase()

		if (settings.isFirst)
		{
			val view_howToUse = LayoutInflater.from(this).inflate(R.layout.dialog_help, NestedScrollView(this), false)
			val textView = view_howToUse.findViewById<TextView>(R.id.autoCleanWarn)
			if (settings.isAutoClean)
			{
				textView.visibility = View.VISIBLE
			}
			AlertDialog.Builder(this)
					.setTitle(" ")
					.setView(view_howToUse)
					.setPositiveButton(R.string.action_done, null)
					.setOnDismissListener {
						val view_license = LayoutInflater.from(this).inflate(R.layout.dialog_license, NestedScrollView(this), false)
						val text_license_point1 = view_license.findViewById<TextView>(R.id.license_point1)
						val text_license_point2 = view_license.findViewById<TextView>(R.id.license_point2)
						val text_license_point3 = view_license.findViewById<TextView>(R.id.license_point3)
						val point = VectorDrawableCompat.create(resources, R.drawable.ic_point, null)
						point!!.setBounds(0, 0, point.minimumWidth, point.minimumHeight)
						text_license_point1.setCompoundDrawables(point, null, null, null)
						text_license_point2.setCompoundDrawables(point, null, null, null)
						text_license_point3.setCompoundDrawables(point, null, null, null)
						AlertDialog.Builder(this)
								.setTitle(" ")
								.setView(view_license)
								.setPositiveButton(R.string.action_done, { _, _ ->
									settings.isFirst = false
								})
								.show()
					}
					.show()
		}
	}

	private fun monitor()
	{
		img_janyo.setOnClickListener {
			val intent = Intent(Intent.ACTION_VIEW)
			intent.data = Uri.parse(getString(R.string.address_home_page))
			startActivity(intent)
		}
	}

	private fun checkPermission()
	{
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this,
					arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
					PERMISSION_CODE)
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean
	{
		Logs.i(TAG, "onCreateOptionsMenu: 创建菜单")
		menuInflater.inflate(R.menu.menu_main, menu)

		val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
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
				invalidateOptionsMenu()
			}
		}
		searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
		searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
		{
			override fun onQueryTextSubmit(query: String): Boolean
			{
				currentFragment.showList.clear()
				if (query.isNotEmpty())
				{
					val searchList = AppManager.searchApps(currentFragment.installAppList, query)
					currentFragment.showList.addAll(searchList)
				}
				else
				{
					currentFragment.showList.addAll(currentFragment.installAppList)
				}
				currentFragment.appRecyclerViewAdapter.notifyDataSetChanged()
				return true
			}

			override fun onQueryTextChange(newText: String): Boolean
			{
				currentFragment.showList.clear()
				if (newText.isNotEmpty())
				{
					val searchList = AppManager.searchApps(currentFragment.installAppList, newText)
					currentFragment.showList.addAll(searchList)
				}
				else
				{
					currentFragment.showList.addAll(currentFragment.installAppList)
				}
				currentFragment.appRecyclerViewAdapter.notifyDataSetChanged()
				return false
			}
		})
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		when (item.itemId)
		{
			android.R.id.home ->
			{
				drawer_layout.openDrawer(GravityCompat.START)
			}
			R.id.action_clear -> Snackbar.make(coordinatorLayout, String.format(getString(R.string.hint_clear_file), (if (JYFileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败")), Snackbar.LENGTH_SHORT)
					.show()
			R.id.action_sort ->
			{
				var index = settings.sort
				AlertDialog.Builder(this)
						.setTitle(R.string.hint_select_sort)
						.setSingleChoiceItems(R.array.sort, index) { _, i -> index = i }
						.setPositiveButton(R.string.action_done) { _, _ ->
							settings.sort = index
							currentFragment.refreshList()
						}
						.show()
			}
			R.id.action_select_all ->
			{
				val list = currentFragment.appRecyclerViewAdapter.multiChoiceList
				list.clear()
				list.addAll(currentFragment.showList)
				currentFragment.appRecyclerViewAdapter.notifyDataSetChanged()
			}
			R.id.action_select_none ->
			{
				val list = currentFragment.appRecyclerViewAdapter.multiChoiceList
				list.clear()
				currentFragment.appRecyclerViewAdapter.notifyDataSetChanged()
				invalidateOptionsMenu()
			}
			R.id.action_export ->
			{
				val list = currentFragment.appRecyclerViewAdapter.multiChoiceList
				spotsDialog.show()
				currentFragment.exportAPK(list, object : ExportListener
				{
					override fun done(finish: Int, error: Int, fileList: ArrayList<File>)
					{
						spotsDialog.dismiss()
						Snackbar.make(coordinatorLayout, getString(R.string.hint_copy_done_with_number, finish, error), Snackbar.LENGTH_SHORT)
								.show()
						currentFragment.exportHandler.sendEmptyMessage(0)
					}
				})
			}
			R.id.action_send ->
			{
				val list = currentFragment.appRecyclerViewAdapter.multiChoiceList
				spotsDialog.show()
				currentFragment.exportAPK(list, object : ExportListener
				{
					override fun done(finish: Int, error: Int, fileList: ArrayList<File>)
					{
						spotsDialog.dismiss()
						Snackbar.make(coordinatorLayout, getString(R.string.hint_copy_done_with_number, finish, error), Snackbar.LENGTH_SHORT)
								.addCallback(object : Snackbar.Callback()
								{
									override fun onDismissed(transientBottomBar: Snackbar?,
															 event: Int)
									{
										JYFileUtil.doShare(this@MainActivity, fileList)
										currentFragment.exportHandler.sendEmptyMessage(0)
									}
								})
								.show()
					}
				})
			}
		}
		return true
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean
	{
		when (item.itemId)
		{
			R.id.action_file_transfer -> startActivity(Intent(this, FileTransferConfigureActivity::class.java), ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
			R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
			R.id.action_license ->
			{
				val view_license = LayoutInflater.from(this).inflate(R.layout.dialog_license, NestedScrollView(this), false)
				val text_license_point1 = view_license.findViewById<TextView>(R.id.license_point1)
				val text_license_point2 = view_license.findViewById<TextView>(R.id.license_point2)
				val text_license_point3 = view_license.findViewById<TextView>(R.id.license_point3)
				val point = VectorDrawableCompat.create(resources, R.drawable.ic_point, null)
				point!!.setBounds(0, 0, point.minimumWidth, point.minimumHeight)
				text_license_point1.setCompoundDrawables(point, null, null, null)
				text_license_point2.setCompoundDrawables(point, null, null, null)
				text_license_point3.setCompoundDrawables(point, null, null, null)
				AlertDialog.Builder(this)
						.setTitle(" ")
						.setView(view_license)
						.setPositiveButton(R.string.action_done, { _, _ ->
							settings.isFirst = false
						})
						.show()
			}
			R.id.action_support ->
			{
				val method = if (isGooglePlayAvailable)
					R.array.pay_method
				else
					R.array.pay_method_without_play
				val list = ArrayList<String>()
				val supportList = resources.getStringArray(R.array.support_list)
				list.addAll(resources.getStringArray(method))
				list.addAll(supportList)
				AlertDialog.Builder(this)
						.setTitle(R.string.pay_method_title)
						.setItems(Array(list.size, { i -> list[i] }), { _, choose ->
							val message = Message()
							when (choose)
							{
								0 ->
								{
									message.what = PayHandler.PAY_ALIPAY
									isGooglePlayPay = false
									payHandler.sendMessage(message)
								}
								1 ->
								{
									message.what = PayHandler.PAY_WEIXIN
									isGooglePlayPay = false
									payHandler.sendMessage(message)
								}
								2 ->
								{
									if (isGooglePlayAvailable)
									{
										message.what = PayHandler.PAY_PLAY
										isGooglePlayPay = true
										payHandler.sendMessage(message)
									}
								}
							}
						})
						.show()
			}
			else -> return true
		}
		drawer_layout.closeDrawer(GravityCompat.START)
		return true
	}

	override fun onBackPressed()
	{
		if (drawer_layout.isDrawerOpen(GravityCompat.START))
		{
			drawer_layout.closeDrawer(GravityCompat.START)
		}
		else
		{
			val doubleClickTime = System.currentTimeMillis()
			if (doubleClickTime - oneClickTime > 2000)
			{
				Snackbar.make(coordinatorLayout, R.string.hint_twice_exit, Snackbar.LENGTH_SHORT)
						.show()
				oneClickTime = doubleClickTime
			}
			else
			{
				finish()
				System.exit(0)//销毁进程
			}
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
											grantResults: IntArray)
	{
		when (requestCode)
		{
			PERMISSION_CODE -> if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
			{
				Snackbar.make(coordinatorLayout, R.string.hint_permission, Snackbar.LENGTH_LONG)
						.setAction(R.string.action_done) { checkPermission() }
						.addCallback(object : Snackbar.Callback()
						{
							override fun onDismissed(transientBottomBar: Snackbar?, event: Int)
							{
								if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
								{
									finish()
								}
							}
						})
						.show()
			}
			else
			{
				if (settings.isAutoClean)
				{
					Snackbar.make(coordinatorLayout, String.format(getString(R.string.hint_clear_file), (if (JYFileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败")), Snackbar.LENGTH_SHORT)
							.show()
				}
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		if (!isGooglePlayPay && payHandler.onPayResult(requestCode, resultCode, data))
			super.onActivityResult(requestCode, resultCode, data)
		else
			Logs.i(TAG, "onActivityResult handled by IABUtil.")
	}

	override fun onDestroy()
	{
		try
		{
			payHandler.playDestroy()
		}
		catch (e: Exception)
		{
			Logs.e(TAG, "onDestroy: 销毁失败")
		}
		super.onDestroy()
	}

	private fun showcase()
	{
		val tapTargetSequence = TapTargetSequence(this)
				.targets(
						TapTarget.forToolbarNavigationIcon(toolbar, "测试toolbar", "描述"),
						TapTarget.forToolbarMenuItem(toolbar, R.id.action_search, "测试按钮", "描述")
				)
				.listener(object : TapTargetSequence.Listener
				{
					override fun onSequenceCanceled(lastTarget: TapTarget?)
					{
						Logs.i(TAG, "onSequenceCanceled: ")
					}

					override fun onSequenceFinish()
					{
						Logs.i(TAG, "onSequenceFinish: ")
					}

					override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean)
					{
						Logs.i(TAG, "onSequenceStep: ")
					}
				})
		tapTargetSequence.start()
	}
}
